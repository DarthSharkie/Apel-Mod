package net.mcbrincie.apel.lib.animators;

import net.mcbrincie.apel.Apel;
import net.mcbrincie.apel.lib.exceptions.SeqDuplicateException;
import net.mcbrincie.apel.lib.exceptions.SeqMissingException;
import net.mcbrincie.apel.lib.objects.ParticleObject;
import net.mcbrincie.apel.lib.renderers.ApelServerRenderer;
import net.mcbrincie.apel.lib.util.interceptor.DrawInterceptor;
import net.mcbrincie.apel.lib.util.interceptor.InterceptData;
import net.mcbrincie.apel.lib.util.scheduler.ScheduledStep;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/** The parallel path animator. Which provides an interface for controlling multiple
 * concurrent path animators (they can also nest themselves) and can have an unlimited
 * number of path animators attached. They also can have delays for each path animator.
 * It is quite advanced but allows for easier management on multiple animators & is
 * versatile compared to the other easier ones
 */
@SuppressWarnings("unused")
public class SequentialAnimator extends PathAnimatorBase implements TreePathAnimator<PathAnimatorBase> {
    protected List<PathAnimatorBase> animators;
    protected List<Integer> animatorDelays;

    protected DrawInterceptor<SequentialAnimator, OnRenderPathAnimator> onAnimatorRendering = DrawInterceptor.identity();

    public enum OnRenderPathAnimator {PATH_ANIMATOR, SHOULD_RENDER_ANIMATOR, DELAY}

    public static <B extends Builder<B>> Builder<B> builder() {
        return new Builder<>();
    }

    private <B extends Builder<B>> SequentialAnimator(Builder<B> builder) {
        this.animators = builder.childAnimators;
        this.animatorDelays = builder.childAnimatorDelays;
    }

    /** Appends a new child path animator to the collection of the child path animators
     * from the particle combiner. The method returns nothing
     *
     * @param animator The path animator to append
    */
    @Override
    public void addAnimatorPath(PathAnimatorBase animator) {
        this.animators.add(animator);
    }

    /** Removes a child path animator from the collection of the child path animators
     * from the particle combiner. The method returns nothing
     *
     * @param animator The path animator to remove
    */
    @Override
    public void removeAnimatorPath(PathAnimatorBase animator) {
        this.animators.remove(animator);
    }

    @Override
    public List<PathAnimatorBase> getPathAnimators() {
        return this.animators;
    }

    @Override
    public PathAnimatorBase getPathAnimator(int index) {
        return this.animators.get(index);
    }

    /** This method is DEPRECATED and SHOULD NOT BE USED */
    @Override
    @Deprecated
    public int setRenderingSteps(int steps) {
        throw new UnsupportedOperationException("Sequential Animators cannot set rendering steps");
    }

    /** This method is DEPRECATED and SHOULD NOT BE USED */
    @Deprecated
    @Override
    public ParticleObject<? extends ParticleObject<?>> setParticleObject(@NotNull ParticleObject<? extends ParticleObject<?>> object) {
        throw new UnsupportedOperationException("Sequential Animators cannot set an individual particle object");
    }

    /** This method is DEPRECATED and SHOULD NOT BE USED */
    @Override
    @Deprecated
    public float setRenderingInterval(float interval) {
        throw new UnsupportedOperationException("Sequential Animators cannot set rendering interval");
    }

    @Override
    public int convertIntervalToSteps() {
        return 0;
    }

    @Override
    protected int calculateDuration() {
        int index = 0;
        int delaySum = 0;
        for (PathAnimatorBase animatorChild : this.animators) {
            int seqDelay = getDelayOfAnimator(index);
            delaySum += seqDelay + animatorChild.calculateDuration();
            index++;
        }
        return delaySum;
    }

    @Override
    public void beginAnimation(ApelServerRenderer renderer) throws SeqDuplicateException, SeqMissingException {
        this.allocateToScheduler();
        int step = 0;
        PathAnimatorBase prev = null;
        for (PathAnimatorBase animator : this.animators) {
            step++;
            InterceptData<OnRenderPathAnimator> interceptData =
                    this.doBeforeStep(renderer.getServerWorld(), animator, getDelayOfAnimator(step), step);
            if (!interceptData.getMetadata(OnRenderPathAnimator.SHOULD_RENDER_ANIMATOR, true)) {
                continue;
            }
            animator = interceptData.getMetadata(OnRenderPathAnimator.PATH_ANIMATOR, animator);
            int delayForAnimator = (int) interceptData.getMetadata(OnRenderPathAnimator.DELAY);
            int delayForAnimatorInUse = this.getDelayOfAnimator(step);
            if (delayForAnimator != delayForAnimatorInUse) {
                this.animatorDelays.set(step - 1, delayForAnimator);
                if (delayForAnimator != this.delay) this.delay = -1;
                for (int delayPerAnimator : this.animatorDelays) {
                    if (delayPerAnimator != delayForAnimator) continue;
                    this.delay = -1;
                    break;
                }
            }
            this.allocateNewAnimator(renderer, step, animator, prev);
            prev = animator;
        }
    }

    private int getDelayOfAnimator(int step) {
        return (this.delay == -1) ? this.animatorDelays.get(step - 1) : this.delay;
    }

    protected void allocateNewAnimator(ApelServerRenderer renderer, int step, PathAnimatorBase animator, PathAnimatorBase prev) {
        Runnable func = () -> animator.beginAnimation(renderer);
        int childDelay = 0;
        if (prev != null) childDelay = prev.calculateDuration();
        int delayUsed = childDelay + getDelayOfAnimator(step);
        if (delayUsed == 0) {
            Apel.DRAW_EXECUTOR.submit(func);
            return;
        }
        if (this.processingSpeed <= 1) {
            Apel.SCHEDULER.allocateNewStep(
                    this, new ScheduledStep(delayUsed, new Runnable[]{func})
            );
            return;
        } else if (step % this.processingSpeed != 0) {
            this.storedFuncsBuffer.add(func);
            return;
        }
        Apel.SCHEDULER.allocateNewStep(
                this, new ScheduledStep(delayUsed, this.storedFuncsBuffer.toArray(Runnable[]::new))
        );
        this.storedFuncsBuffer.clear();
    }

    /** Set the interceptor to run before the drawing of each individual rendering step. The interceptor will be provided
     * with references to the {@link ServerWorld}, the current step number. As far as it goes for the metadata, you
     * have access to the path animator that will be drawn, the delay of the path animator before rendering and a
     * boolean value dictating if the path animator should render at all
     *
     * @param duringRenderingSteps the new interceptor to execute before drawing the individual steps
     */
    public void setOnAnimatorRendering(DrawInterceptor<SequentialAnimator, OnRenderPathAnimator> duringRenderingSteps) {
        this.onAnimatorRendering = Optional.ofNullable(duringRenderingSteps).orElse(DrawInterceptor.identity());
    }

    protected InterceptData<OnRenderPathAnimator> doBeforeStep(
            ServerWorld world, PathAnimatorBase pathAnimatorBase, int delay, int currStep
    ) {
        InterceptData<OnRenderPathAnimator> interceptData = new InterceptData<>(
                world, null, currStep, OnRenderPathAnimator.class
        );
        interceptData.addMetadata(OnRenderPathAnimator.PATH_ANIMATOR, pathAnimatorBase);
        interceptData.addMetadata(OnRenderPathAnimator.DELAY, delay);
        interceptData.addMetadata(OnRenderPathAnimator.SHOULD_RENDER_ANIMATOR, true);
        this.onAnimatorRendering.apply(interceptData, this);
        return interceptData;
    }

    public static class Builder<B extends Builder<B>> extends PathAnimatorBase.Builder<B, SequentialAnimator> {
        protected List<PathAnimatorBase> childAnimators = new ArrayList<>();
        protected List<Integer> childAnimatorDelays = new ArrayList<>();

        private Builder () {}

        public B animator(PathAnimatorBase animator) {
            this.childAnimators.add(animator);
            return self();
        }

        public B animator(PathAnimatorBase animator, int delay) {
            this.childAnimators.add(animator);
            this.childAnimatorDelays.add(delay);
            return self();
        }

        public B animators(List<PathAnimatorBase> animators) {
            this.childAnimators.addAll(animators);
            return self();
        }

        public B animators(List<PathAnimatorBase> animators, List<Integer> delays) {
            this.childAnimators.addAll(animators);
            this.childAnimatorDelays.addAll(delays);
            return self();
        }

        @Override
        public SequentialAnimator build() {
            if (this.delay < 0) {
                throw new IllegalStateException("Initial delay must be non-negative");
            }
            for (int i = 0; i < this.childAnimators.size(); i++) {
                if (this.childAnimators.get(i) == null) {
                    throw new NullPointerException("Child Animator cannot be null");
                }
                // Pad the list of delays, so it's equal in length
                if (this.childAnimatorDelays.size() == i) {
                    this.childAnimatorDelays.add(0);
                }
                if (this.childAnimatorDelays.get(i) < 0) {
                    throw new IllegalStateException("Child animator delays must be non-negative");
                }
            }
            return new SequentialAnimator(this);
        }
    }
}
