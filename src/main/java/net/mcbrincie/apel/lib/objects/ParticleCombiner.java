package net.mcbrincie.apel.lib.objects;

import net.mcbrincie.apel.lib.renderers.ApelServerRenderer;
import net.mcbrincie.apel.lib.util.interceptor.DrawInterceptor;
import net.mcbrincie.apel.lib.util.interceptor.InterceptData;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** A utility particle object class that groups all particle objects as
 * one object instead of multiple. Particle combiners can also group themselves
 * which can produce an object hierarchy. There are many good things about using
 * a particle combiner in most cases, examples include but are not limited to
 * <br><br>
 * <center><h2>Advantages</h2></center>
 * <br>
 *
 * <b>Fewer Memory Overhead(s) & Smaller Memory Footprint</b><br>
 * Since objects are grouped together. This means that there will be fewer particle animators created
 * as well as the object being handled as one particle object instance instead of being multiple. Which
 * means that memory is dramatically reduced down for complex scenes (if you had 10 path animators for 10
 * objects, in which the path animators do 1 million rendering steps. The entire bandwidth is cut down
 * from 10 million -> 1 million steps allocated to the scheduler for the processing).<br><br>
 *
 * <b>Easier Management On Multiple Complex Objects</b><br>
 * The main premise of the particle combiner is to combine particle objects as 1. Which can simplify
 * repetitive logic and instead of passing the objects into separate path animators (that contain the
 * almost same params and are the same type). Now you can pass it in only one path animator, there are
 * common methods for managing the object instances which further simplify the repetitive process.<br><br>
 *
 * <b>Dynamic Object Allocation At Runtime</b><br>
 * Without the particle combiner, it is challenging to create objects at runtime and create a new path animator
 * that inherits almost all the same attributes as all the other animators for the other objects & programmatically
 * changing the params is very tedious. This doesn't have to be the case, because you can allocate a new particle
 * object to the particle combiner and from there APEL would take care the rest.<br><br>
 *
 * <b>Controlling Objects Before Being Drawn</b><br>
 * Developers may use {@link #setBeforeChildDraw(DrawInterceptor)} to control the object itself before other
 * interceptors from that object apply.  They may also choose whether to draw the object or not by modifying
 * {@code CAN_DRAW_OBJECT}, which this logic is not possible without changing the particle object's class.<br><br>
 *
 * <b>Hierarchical Grouping</b><br>
 * Particle combiners can also combine themselves which can allow for the creation of tree-like particle hierarchies,
 * without the need of making your own particle object class and configuring the params to work that way. All
 * this logic you would have to go and implement is handled for you. You can also use recursive
 * methods to scale down the tree and modify the values<br><br>
 *
 * @param <T> The type of the object can also be set to <?> to accept all particle objects
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ParticleCombiner<T extends ParticleObject> extends ParticleObject {
    protected List<T> objects = new ArrayList<>();
    protected int amount = -1;

    public DrawInterceptor<ParticleCombiner<T>, AfterChildDrawData> afterChildDraw = DrawInterceptor.identity();
    public DrawInterceptor<ParticleCombiner<T>, BeforeChildDrawData> beforeChildDraw = DrawInterceptor.identity();

    /** There is no data being transmitted */
    public enum AfterChildDrawData {}

    /** This data is used after calculations (it contains the modified four vertices) */
    public enum BeforeChildDrawData {
        OBJECT_IN_USE, CAN_DRAW_OBJECT
    }

    /** The constructor for the particle combiner. Which is a utility class that
     * helps in grouping particle objects together as one single particle object.
     * Which, of course, has many benefits, such as being able to directly modify
     * the objects themselves without needing to set one after the other to a
     * specific value. There is a simpler constructor for no rotation
     * <br><br>
     * <b>Note:</b> it uses the {@code setRotation} which sets all the
     * particle object's rotation values to the provided rotation value
     *
     * @param rotation The rotation to apply
     * @param objects The objects to group together
     *
     * @see ParticleCombiner#ParticleCombiner(Vector3f, ParticleObject[])
     * @see ParticleCombiner#ParticleCombiner(List)
     */
    public ParticleCombiner(Vector3f rotation, List<T> objects) {
        super((ParticleEffect) null); // We do not care about the particle
        this.setObjects(objects);
        this.setRotation(rotation);
    }

    /** The constructor for the particle combiner. Which is a utility class that
     * helps in grouping particle objects together as one single particle object.
     * Which, of course, has many benefits, such as being able to directly modify
     * the objects themselves without needing to set one after the other to a
     * specific value. There is a simpler constructor for no rotation
     * <br><br>
     * <b>Note:</b> it uses the {@code setRotation} which sets all the
     * particle object's rotation values to the provided rotation value
     *
     * @param rotation The rotation to apply
     * @param objects The objects to group together
     *
     * @see ParticleCombiner#ParticleCombiner(ParticleObject[])
     * @see ParticleCombiner#ParticleCombiner(Vector3f, List)
     */
    @SafeVarargs
    public ParticleCombiner(Vector3f rotation, T... objects) {
        this(rotation, Arrays.asList(objects));
    }

    /** The constructor for the particle combiner. Which is a utility class that
     * helps in grouping particle objects together as one single particle object.
     * Which, of course, has many benefits, such as being able to directly modify
     * the objects themselves without needing to set one after the other to a
     * specific value. There is a simpler constructor for no rotation
     * <br><br>
     * <b>Note:</b> it uses the {@code setRotation} which sets all the
     * particle object's rotation values to the provided rotation value
     *
     * @param objects The objects to group together
     *
     * @see ParticleCombiner#ParticleCombiner(ParticleObject[])
     */
    public ParticleCombiner(List<T> objects) {
        this(new Vector3f(0), objects);
    }

    /** The constructor for the particle combiner. Which is a utility class that
     * helps in grouping particle objects together as one single particle object.
     * Which, of course, has many benefits, such as being able to directly modify
     * the objects themselves without needing to set one after the other to a
     * specific value. There is a more complex constructor for rotation
     *
     * @param objects The objects to group together
     *
     * @see ParticleCombiner#ParticleCombiner(Vector3f, ParticleObject[])
    */
    @SafeVarargs
    public ParticleCombiner(T... objects) {
        this(new Vector3f(0), objects);
    }

    /** The copy constructor for the particle combiner. Which
     * copies all the object, offset, and interceptor references into a brand-new
     * instance of the particle combiner.
     *
     * @param combiner The particle combiner to copy from
     */
    public ParticleCombiner(ParticleCombiner<T> combiner) {
        super(combiner);
        this.objects = new ArrayList<>(combiner.objects);
        this.amount = combiner.amount;
        this.afterChildDraw = combiner.afterChildDraw;
        this.beforeChildDraw = combiner.beforeChildDraw;
    }

    /** Sets the rotation for all the particle objects. There is
     * another method that allows for offsetting the rotation per particle
     * object, it is the same as using this one with the offset params.
     *
     * @param rotation The new rotation (IN RADIANS)
     * @return The previous rotation used
     *
     * @see ParticleCombiner#setRotation(Vector3f, float, float, float)
     * @see ParticleCombiner#setRotation(Vector3f, Vector3f)
    */
    @Override
    public Vector3f setRotation(Vector3f rotation) {
        Vector3f prevRotation = new Vector3f(this.rotation);
        // Defensive copy happens in superclass method
        super.setRotation(rotation);
        for (T object : this.objects) {
            // Defensive copy happens in superclass method
            object.setRotation(rotation);
        }
        return prevRotation;
    }

    /** Sets the rotation for all the particle objects, there
     * is an offset of XYZ per object. There is also a simplified
     * method that doesn't use offsets.
     *
     * @param rotation The new rotation (IN RADIANS)
     * @param offsetX the offset x
     * @param offsetY the offset y
     * @param offsetZ the offset z
     * @return The previous rotation
     *
     * @see ParticleCombiner#setRotation(Vector3f)
     * @see ParticleCombiner#setRotation(Vector3f, Vector3f)
     */
    public Vector3f setRotation(Vector3f rotation, float offsetX, float offsetY, float offsetZ) {
        if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
            throw new IllegalArgumentException("offset rotation must not equal (0, 0, 0)");
        }
        Vector3f prevRotation = new Vector3f(this.rotation);
        // Defensive copy happens in superclass method
        super.setRotation(rotation);
        Vector3f rotationToOffset = new Vector3f(rotation);
        for (T object : this.objects) {
            // Defensive copy happens in ParticleObject#setRotation
            object.setRotation(rotationToOffset.add(offsetX, offsetY, offsetZ));
        }
        return prevRotation;
    }

    /** Sets the rotation for all the particle objects, there
     * is an offset of XYZ per object. There is also a simplified
     * method that doesn't use offsets.
     *
     * @param rotation The new rotation (IN RADIANS)
     * @param offset the offset x
     * @return The previous rotation
     *
     * @see ParticleCombiner#setRotation(Vector3f)
     * @see ParticleCombiner#setRotation(Vector3f, float, float, float)
     */
    public Vector3f setRotation(Vector3f rotation, Vector3f offset) {
        return this.setRotation(rotation, offset.x, offset.y, offset.z);
    }

    /** Sets the rotation for all {@code ParticleObject}s in the hierarchy, which can nest additional
     * {@code ParticleCombiner}s. There is another method that allows
     * for offsetting the rotation per particle object, it is the same
     * as using this one with the offset params.
     *
     * @param rotation The new rotation (IN RADIANS)
     *
     * @see ParticleCombiner#setRotationRecursively(Vector3f)
     */
    public void setRotationRecursively(Vector3f rotation) {
        for (T object : this.objects) {
            if (object instanceof ParticleCombiner<?> combiner) {
                combiner.setRotationRecursively(rotation);
            }
            // Defensive copy happens in ParticleObject#setRotation
            object.setRotation(rotation);
        }
    }

    /** Sets the rotation for all {@code ParticleObject}s in the hierarchy, which can nest additional
     * {@code ParticleCombiner}s. There is an offset of XYZ per object. There is as well a
     * simplified method that doesn't use offsets.
     *
     * @param rotation The new rotation (IN RADIANS)
     * @param offset the offset rotation
     *
     * @see ParticleCombiner#setRotationRecursively(Vector3f)
     */
    public void setRotationRecursively(Vector3f rotation, Vector3f offset) {
        if (offset.equals(new Vector3f())) {
            throw new IllegalArgumentException("offset rotation must not equal (0, 0, 0)");
        }
        Vector3f rotationToOffset = new Vector3f(rotation);
        for (T object : this.objects) {
            if (object instanceof ParticleCombiner<?> combiner) {
                combiner.setRotationRecursively(rotationToOffset.add(offset), offset);
            }
            // Defensive copy happens in ParticleObject#setRotation
            object.setRotation(rotationToOffset.add(offset));
        }
    }

    /** Gets the offsets per object and returns a list.  The resulting list can be modified in-place.
     *
     * @return The list of offsets per object
     */
    public List<Vector3f> getOffsets() {
        List<Vector3f> offsets = new ArrayList<>(this.objects.size());
        for (T obj : this.objects) {
            offsets.add(obj.getOffset());
        }
        return offsets;
    }

    /** Get the offset for the object at the given index.  The resulting offset can be modified in-place.
     *
     * @param index The index of the object
     * @return The offset of the object at the given index
     */
    public Vector3f getOffset(int index) {
        return this.objects.get(index).getOffset();
    }

    /** Sets the offset position per object. The offset positions have to be the same
     * amount as the objects.  If a null element exists, the offset will be set to (0, 0, 0).
     * There is a helper method to allow setting for all objects the same offset.
     *
     * @param offsets The offsets for each object (corresponding on each index)
     * @return The previous offsets
     */
    public List<Vector3f> setOffsets(Vector3f... offsets) {
        if (offsets.length != this.objects.size()) {
            throw new IllegalArgumentException("Must provide an offset for every object");
        }
        List<Vector3f> prevOffsets = this.getOffsets();
        for (int i = 0; i < this.objects.size(); i++) {
            this.objects.get(i).setOffset(Optional.ofNullable(offsets[i]).orElse(new Vector3f()));
        }
        return prevOffsets;
    }

    /**
     * Sets the offset position to all objects. The offset applies to all objects and overwrites
     * the values. New objects added will have their offset to (0,0,0). There is a
     * helper method to allow setting different offsets to different objects.
     *
     * @param offset The offsets for all the objects
     * @return The previous offsets
     */
    public List<Vector3f> setOffsets(Vector3f offset) {
        List<Vector3f> prevOffsets = this.getOffsets();
        Vector3f newOffset = Optional.ofNullable(offset).orElse(new Vector3f());
        for (T object : this.objects) {
            // Ensure every offset has a unique reference, so a future modification doesn't impact all of them
            object.setOffset(new Vector3f(newOffset));
        }
        return prevOffsets;
    }

    /** Sets the offset position for the individual object by supplying an index and the
     * new offset for that object.
     *
     * @param offset The offsets for the indexed object
     * @return The previous offset
     */
    public Vector3f setOffset(int index, Vector3f offset) {
        return this.objects.get(index).setOffset(new Vector3f(offset));
    }

    /** Sets the offset position for the individual object by supplying the object and the
     * new offset for that object. If the object is not found, it will return null.
     *
     * @param offset The offsets for the individual object
     * @return The previous offset
     */
    public Vector3f setOffset(T object, Vector3f offset) {
        int index = this.objects.indexOf(object);
        if (index == -1) {
            return null;
        }
        return this.setOffset(index, offset);
    }

    /** Sets the particle to use to a new value and returns the previous particle that was used.
     * This applies to all the object the value can also be null, meaning that there are different
     * particle effects at play in the object.
     *
     * @param particle The new particle
     * @return The previous particle
    */
    @Override
    public ParticleEffect setParticleEffect(ParticleEffect particle) {
        ParticleEffect prevParticle = super.setParticleEffect(particle);
        for (T object : this.objects) {
            object.setParticleEffect(particle);
        }
        return prevParticle;
    }

    /** Sets the particle to use to a new value and returns the previous particle that was used.
     * This applies to all the objects (including objects that are way below the hierarchy). The value
     * can also be null, meaning that there are different particle effects at play in the object.
     *
     * @param particle The new particle
     * @return The previous particle
     *
     * @see ParticleCombiner#setParticleEffectRecursively(ParticleEffect[])
     */
    public ParticleEffect setParticleEffectRecursively(ParticleEffect particle) {
        ParticleEffect prevParticle = super.setParticleEffect(particle);
        for (T object : this.objects) {
            if (object instanceof ParticleCombiner<?> combiner) {
                combiner.setParticleEffectRecursively(particle);
            } else {
                object.setParticleEffect(particle);
            }
        }
        return prevParticle;
    }

    /** Sets the particle to use to a new value and returns the previous particle that was used.
     * This applies to all the objects (including objects that are way below the hierarchy). The value
     * can also be null, meaning that there are different particle effects at play in the object.
     * However unlike the {@code setParticleEffectRecursively(ParticleEffect)} which is a list of
     * the particles to use per depth level.
     *
     * <p><b>Note:</b> If there are no more particles to supply in the current depth level the system is,
     * it will not go deeper.
     *
     * @param particleEffects The particle effects for each level
     *
     * @see ParticleCombiner#setParticleEffectRecursively(ParticleEffect)
     */
    public void setParticleEffectRecursively(ParticleEffect[] particleEffects) {
        this.particleEffect = null;
        this.particleEffectRecursiveLogic(particleEffects, 0);
    }

    private void particleEffectRecursiveLogic(ParticleEffect[] particleEffects, int depth) {
        for (T object : this.objects) {
            if (depth >= particleEffects.length) break;
            if (object instanceof ParticleCombiner<?> combiner) {
                object.particleEffect = null;
                combiner.particleEffectRecursiveLogic(particleEffects, depth + 1);
            }
            object.setParticleEffect(particleEffects[depth]);
        }
    }

    /** Sets the amount to use to a new value and returns the previous amount that was used.
     * This applies to all the objects. The value can also be -1 meaning that there are different
     * particle effects at play in the object. There is a method that allows for offsets per
     * particle objects.
     *
     * @param amount The new particle
     * @return The previous particle
     *
     * @see ParticleCombiner#setAmount(int, int)
     * @see ParticleCombiner#setAmountRecursively(int, int)
     * @see ParticleCombiner#setAmountRecursively(int, int, int)
    */
    @Override
    public int setAmount(int amount) {
        int prevAmount = super.setAmount(amount);
        for (T object : this.objects) {
            object.setAmount(amount);
        }
        return prevAmount;
    }

    /** Sets the amount to use to a new value and returns the previous amount that was used.
     * This applies to all the objects. The value can also be -1 meaning that there are different
     * particle effects at play in the object. The offset param changes the amount per object by
     * a specified amount. There is also a simplified version that doesn't use offsets.
     *
     * @param amount The new particle
     * @param offset The offset of the amount (can be positive or negative)
     * @return The previous particle
     *
     * @see ParticleCombiner#setAmount(int)
     * @see ParticleCombiner#setAmountRecursively(int, int)
     * @see ParticleCombiner#setAmountRecursively(int, int, int)
     */
    public int setAmount(int amount, int offset) {
        if (offset == 0) {
            throw new IllegalArgumentException("offset must not equal to 0");
        }
        int prevAmount = super.setAmount(amount);
        int i = 0;
        for (T object : this.objects) {
            object.setAmount(amount + (offset * i));
            i++;
        }
        return prevAmount;
    }

    /** Sets the amount to use to a new value and returns the previous amount that was used.
     * This applies to all the objects (including objects that are way below the hierarchy). The returned
     * value can also be -1 meaning that there are different particle effects at play in the object.
     * The offset param changes the amount per object by a specified amount. There is also a simplified version
     * that doesn't recursively scale down the tree & another that allows specifying the recursion offset.
     *
     * <p><b>Note:</b> When the program encounters another combiner, it calls the same method, but
     * the amount is added with the iterated offset and the offset remains unchanged
     *
     * @param amount The new particle
     * @param offset The offset of the amount (can be positive or negative)
     *
     * @see ParticleCombiner#setAmountRecursively(int, int, int)
     * @see ParticleCombiner#setAmount(int)
     * @see ParticleCombiner#setAmount(int, int)
     */
    public void setAmountRecursively(int amount, int offset) {
        if (offset == 0) {
            throw new IllegalArgumentException("offset must not equal to 0");
        }
        int i = -1;
        for (T object : this.objects) {
            i++;
            if (object instanceof ParticleCombiner<?> combiner) {
                combiner.setAmountRecursively(amount + (offset * i), offset);
            }
            object.setAmount(amount + (offset * i));
        }
        this.amount = -1;
    }

    /** Sets the amount to use to a new value and returns the previous amount that was used.
     * This applies to all the objects (including objects that are way below the hierarchy). The returned
     * value can also be -1 meaning that there are different particle effects at play in the object.
     * The offset param changes the amount per object by a specified amount. There is also a simplified version
     * that doesn't recursively scale down the tree & another that doesn't accept the recursion offset.
     *
     * <p><b>Note:</b> when the program encounters another combiner. It calls the same method, but
     * the amount is added with the recursive offset and the recursive offset remains unchanged
     *
     * @param amount The new particle
     * @param offset The offset of the amount (can be positive or negative)
     * @param recursiveOffset The offset of the amount once the program encounters another combiner
     *
     * @see ParticleCombiner#setAmountRecursively(int, int)
     * @see ParticleCombiner#setAmount(int)
     * @see ParticleCombiner#setAmount(int, int)
     */
    public void setAmountRecursively(int amount, int offset, int recursiveOffset) {
        if (offset == 0) {
            throw new IllegalArgumentException("Normal Offset must not equal to 0");
        }
        if (recursiveOffset == 0) {
            throw new IllegalArgumentException("Recursive offset must not equal to 0");
        }
        int i = -1;
        for (T object : this.objects) {
            i++;
            if (object instanceof ParticleCombiner<?> combiner) {
                combiner.setAmountRecursively(amount + (recursiveOffset * i), recursiveOffset);
            }
            object.setAmount(amount + (offset * i));
        }
        this.amount = -1;
    }

    /** Gets the list of particle objects and returns them.
     *
     * @return The list of particle objects
     */
    public List<T> getObjects() {
        return this.objects;
    }

    /** Gets the particle objects at that index and returns it.
     *
     * @param index the index of the particle object
     * @return The list of particle objects
     */
    public T getObject(int index) {
        return objects.get(index);
    }

    /** Sets the number of particle objects to use and returns the previous objects that were used.
     *
     * @param objects The particle objects list
     * @return The previous particle objects list
    */
    @SafeVarargs
    public final List<T> setObjects(T... objects) {
        return this.setObjects(Arrays.asList(objects));
    }

    /** Sets the number of particle objects to use and returns the previous objects that were used.
     *
     * @param objects The particle objects list
     * @return The previous particle objects list
     */
    public final List<T> setObjects(List<T> objects) {
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("There has to be at least one object supplied");
        }
        List<T> prevObjects = this.objects;

        // Defensive copy (and to guarantee internal mutability)
        this.objects = new ArrayList<>(objects);

        for (T object : this.objects) {
            if (this.amount == -1 && this.particleEffect == null) break;
            this.amount = (object.amount != this.amount) ? -1 : this.amount;
            this.particleEffect = (object.particleEffect != this.particleEffect) ? null : this.particleEffect;
        }
        return prevObjects;
    }

    /** Sets the individual object at that index to a different object.
     *
     * @param index The index of the particle object to replace at
     * @param newObject The new particle object
     * @return The previous particle object
     */
    public T setObject(int index, T newObject) {
        T prevObject = this.objects.set(index, newObject);
        if (this.particleEffect != newObject.particleEffect) {
            this.particleEffect = null;
        }
        if (this.amount != newObject.amount) {
            this.amount = -1;
        }
        return prevObject;
    }

    /** Adds all the objects at the back of the list.
     *
     * @param objects The objects to add
     */
    @SafeVarargs
    public final void appendObjects(T... objects) {
        List<T> objectList = Arrays.stream(objects).toList();
        this.objects.addAll(objectList);
        for (T object : objects) {
            if (object.amount != this.amount) this.amount = -1;
            if (object.particleEffect != this.particleEffect) this.particleEffect = null;
        }
    }

    /** Appends a new particle object to the combiner. This is at the back
     *  of the list. Meaning that this object is the last one in the list.
     *  The offset position is (0,0,0) when appending, although you can use
     *  the same method by just supplying the offset.
     *
     * @param object The object to add to the list
     */
    public void appendObject(T object) {
        if (object.amount != this.amount) this.amount = -1;
        if (object.particleEffect != this.particleEffect) this.particleEffect = null;
        this.objects.add(object);
    }

    /** Removes an object from the combiner.
     *
     * @param index The index to remove at
     * @return The removed object
     */
    public T removeObject(int index) {
        return this.objects.remove(index);
    }

    /** Removes an object from the combiner.
     *
     * @param object The object to remove
     * @return The removed object
     */
    public T removeObject(T object) {
        int index = this.objects.indexOf(object);
        return this.objects.remove(index);
    }

    @Override
    public void draw(ApelServerRenderer renderer, int step, Vector3f drawPos) {
        int index = -1;
        for (T object : this.objects) {
            index++;
            InterceptData<BeforeChildDrawData> interceptData = this.doBeforeChildDraw(renderer.getServerWorld(), step, object);
            boolean shouldDraw = interceptData.getMetadata(BeforeChildDrawData.CAN_DRAW_OBJECT, true);
            if (!shouldDraw) {
                continue;
            }
            ParticleObject childObject = interceptData.getMetadata(BeforeChildDrawData.OBJECT_IN_USE, object);
            // Defensive copy before passing to a child object
            Vector3f childDrawPos = new Vector3f(drawPos);
            childObject.draw(renderer, step, childDrawPos);
            this.doAfterChildDraw(renderer.getServerWorld(), step);
        }
    }

    /**
     * Set the interceptor to run before drawing each child object.  The interceptor will be provided
     * with references to the {@link ServerWorld}, the step number of the animation, the child object
     * to be rendered, and a boolean describing whether it should be rendered.
     *
     * @param beforeChildDrawIntercept The interceptor to execute before drawing each child object
     */
    public void setBeforeChildDraw(DrawInterceptor<ParticleCombiner<T>, BeforeChildDrawData> beforeChildDrawIntercept) {
        this.beforeChildDraw = Optional.ofNullable(beforeChildDrawIntercept).orElse(DrawInterceptor.identity());
    }

    /**
     * Set the interceptor to run after drawing each child object.  The interceptor will be provided
     * with references to the {@link ServerWorld} and the step number of the animation.
     *
     * @param afterChildDraw The interceptor to execute after drawing each child object
     */
    public void setAfterChildDraw(DrawInterceptor<ParticleCombiner<T>, AfterChildDrawData> afterChildDraw) {
        this.afterChildDraw = Optional.ofNullable(afterChildDraw).orElse(DrawInterceptor.identity());
    }

    private void doAfterChildDraw(ServerWorld world, int step) {
        InterceptData<AfterChildDrawData> interceptData = new InterceptData<>(world, null, step, AfterChildDrawData.class);
        this.afterChildDraw.apply(interceptData, this);
    }

    private InterceptData<BeforeChildDrawData> doBeforeChildDraw(ServerWorld world, int step, T objectInUse) {
        InterceptData<BeforeChildDrawData> interceptData = new InterceptData<>(world, null, step, BeforeChildDrawData.class);
        interceptData.addMetadata(BeforeChildDrawData.OBJECT_IN_USE, objectInUse);
        interceptData.addMetadata(BeforeChildDrawData.CAN_DRAW_OBJECT, true);
        this.beforeChildDraw.apply(interceptData, this);
        return interceptData;
    }
}
