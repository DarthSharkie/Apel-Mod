package net.mcbrincie.apel.lib.objects;

import net.mcbrincie.apel.lib.util.interceptor.DrawInterceptor;
import net.mcbrincie.apel.lib.util.interceptor.InterceptData;
import net.mcbrincie.apel.lib.util.interceptor.InterceptedResult;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3f;

/** A utility particle object class that groups all particle objects as
 * one object instead of multiple. Particle combiners can also group themselves
 * which can produce an object hierarchy. There many good things about using
 * a particle combiner in most cases, examples include but are not limited to
 *
 * <table border="1">
 *   <tr>
 *     <td> cell 11 </td> <td> cell 21</td>
 *   </tr>
 *   <tr>
 *     <td> cell 12 </td> <td> cell 22</td>
 *   </tr>
 * </table>
 *
 * @param <T> The type of the object, can also be set to <?> to accept all particle objects
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ParticleCombiner<T extends ParticleObject> extends ParticleObject {
    protected T[] objects;
    protected int amount = -1;

    public DrawInterceptor<ParticleCombiner<T>, emptyData> afterChildRenderIntercept;
    public DrawInterceptor<ParticleCombiner<T>, beforeChildRenderData> beforeChildRenderIntercept;

    @Deprecated public final DrawInterceptor<?, ?> afterCalcsIntercept = null;
    @Deprecated public final DrawInterceptor<?, ?> beforeCalcsIntercept = null;

    /** There is no data being transmitted */
    public enum emptyData {}

    /** This data is used after calculations(it contains the modified 4 vertices) */
    public enum beforeChildRenderData {
        OBJECT_IN_USE
    }

    /** The constructor for the particle combiner. Which is a utility class that
     * helps in grouping particle objects together as 1 single particle object.
     * Which of course has many benefits such as being able to directly modify
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
     */
    @SafeVarargs
    public ParticleCombiner(Vector3f rotation, T... objects) {
        super(ParticleTypes.SCRAPE); // We do not care about the particle
        this.particle = null;
        this.setObjects(objects);
        this.setRotation(rotation);
    }

    /** The constructor for the particle combiner. Which is a utility class that
     * helps in grouping particle objects together as 1 single particle object.
     * Which of course has many benefits such as being able to directly modify
     * the objects themselves without needing to set one after the other to a
     * specific value. There is a more complex constructor for rotation
     *
     * @param objects The objects to group together
     *
     * @see ParticleCombiner#ParticleCombiner(Vector3f, ParticleObject[])
    */
    @SafeVarargs
    public ParticleCombiner(T... objects) {
        super(ParticleTypes.SCRAPE); // We do not care about the particle
        this.particle = null;
        this.setObjects(objects);
    }

    /** The copy constructor for the particle combiner. Which
     * copies all the objects and the interceptors into a brand-new
     * instance of the particle combiner
     *
     * @param combiner The particle combiner to copy from
     */
    public ParticleCombiner(ParticleCombiner<T> combiner) {
        super(combiner);
        this.objects = combiner.objects;
        this.amount = combiner.amount;
        this.afterChildRenderIntercept = combiner.afterChildRenderIntercept;
        this.beforeChildRenderIntercept = combiner.beforeChildRenderIntercept;
    }

    /** Sets the rotation for all the particle objects. There is
     * another method that allows for offsetting the rotation per particle
     * object, it is the same as using this one with the offset params
     *
     * @param rotation The new rotation(IN RADIANS)
     * @return The previous rotation used
     *
     * @see ParticleCombiner#setRotation(Vector3f, float, float, float)
     * @see ParticleCombiner#setRotation(Vector3f, Vector3f)
    */
    @Override
    public Vector3f setRotation(Vector3f rotation) {
        Vector3f prevRotation = this.rotation;
        this.rotation = rotation;
        for (T object : objects) {
            object.setRotation(this.rotation);
        }
        return prevRotation;
    }

    /** Sets the rotation for all the particle objects, there
     * is an offset of XYZ per object. There is also a simplified
     * method that doesn't use offsets
     *
     * @param rotation The new rotation(IN RADIANS)
     * @param offsetX the offset x
     * @param offsetY the offset y
     * @param offsetZ the offset z
     * @return The previous rotation
     *
     * @see ParticleCombiner#setRotation(Vector3f)
     */
    public Vector3f setRotation(Vector3f rotation, float offsetX, float offsetY, float offsetZ) {
        if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
            throw new IllegalArgumentException("offset rotation must not equal (0, 0, 0)");
        }
        Vector3f prevRotation = this.rotation;
        this.rotation = rotation;
        int i = 0;
        for (T object : objects) {
            object.setRotation(this.rotation.add(offsetX * i, offsetY * i, offsetZ * i));
            i++;
        }
        return prevRotation;
    }

    /** Sets the rotation for all the particle objects, there
     * is an offset of XYZ per object. There is also a simplified
     * method that doesn't use offsets
     *
     * @param rotation The new rotation(IN RADIANS)
     * @param offset the offset x
     * @return The previous rotation
     *
     * @see ParticleCombiner#setRotation(Vector3f)
     */
    public Vector3f setRotation(Vector3f rotation, Vector3f offset) {
        if (offset.equals(new Vector3f())) {
            throw new IllegalArgumentException("offset rotation must not equal (0, 0, 0)");
        }
        Vector3f prevRotation = super.setRotation(rotation);
        int i = 0;
        for (T object : objects) {
            object.setRotation(this.rotation.add(offset.x * i, offset.y * i, offset.z * i));
            i++;
        }
        return prevRotation;
    }

    /** Sets the particle to use to a new value and returns the previous particle that was used.
     * This applies to all the object. The value can also be null meaning that there are different
     * particle effects at play in the object
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

    /** Sets the amount to use to a new value and returns the previous amount that was used.
     * This applies to all the object. The value can also be -1 meaning that there are different
     * particle effects at play in the object. There is a method that allows for offsets per
     * particle objects
     *
     * @param amount The new particle
     *
     * @return The previous particle
     *
     * @see ParticleCombiner#setAmount(int, int)
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
     * This applies to all the object. The value can also be -1 meaning that there are different
     * particle effects at play in the object. The offset param changes the amount per object by
     * a specified amount. There is also a simplified version that doesn't use offsets
     *
     * @param amount The new particle
     * @param offset The offset of the amount(can be positive or negative)
     * @return The previous particle
     *
     * @see ParticleCombiner#setAmount(int)
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


    /** Sets the amount of particle objects to use and returns the previous objects that were used.
     *
     * @param objects The particle objects list
     * @return The previous particle objects list
    */
    public T[] setObjects(T[] objects) {
        if (objects.length <= 1) {
            throw new IllegalArgumentException("There has to be needs than 1 object supplied");
        }
        T[] prevObjects = this.objects;
        this.objects = objects;
        int prevAmount = objects[0].amount;
        ParticleEffect prevEffect = objects[0].particle;
        for (T object : objects) {
            if (this.amount == -1 && this.particle == null) break;
            this.amount = (object.amount != this.amount) ? -1 : this.amount;
            this.particle = (object.particle != this.particle) ? null : this.particle;
        }
        return prevObjects;
    }

    /** Sets the individual object at that index to a different object
     *
     * @param index The index of the particle object to replace at
     * @param newObject The new particle object
     * @return The previous particle object
     */
    public T setIndividualObject(int index, T newObject) {
        T prevObject = this.objects[index];
        this.objects[index] = newObject;
        if (this.particle != newObject.particle) {
            this.particle = null;
        }
        if (this.amount != newObject.amount) {
            this.amount = -1;
        }
        return prevObject;
    }

    /** Sets the rotation for all the particle objects(this includes objects
     * that are way below the hierarchy). There is another method that allows
     * for offsetting the rotation per particle object, it is the same
     * as using this one with the offset params
     *
     * @param rotation The new rotation(IN RADIANS)
     *
     * @see ParticleCombiner#setRotation(Vector3f, float, float, float)
     * @see ParticleCombiner#setRotation(Vector3f, Vector3f)
     */
    public void setRotationRecursively(Vector3f rotation) {
        for (T object : objects) {
            if (object instanceof ParticleCombiner<?> combiner) {
                combiner.setRotationRecursively(rotation);
            }
            object.setRotation(rotation);
        }
    }

    /** Sets the rotation for all the particle objects(including the objects that are
     * below the hierarchy), there is an offset of XYZ per object. There is as well a
     * simplified method that doesn't use offsets
     *
     * @param rotation The new rotation(IN RADIANS)
     * @param offset the offset rotation
     *
     * @see ParticleCombiner#setRotationRecursively(Vector3f)
     */
    public void setRotationRecursively(Vector3f rotation, Vector3f offset) {
        if (offset.equals(new Vector3f())) {
            throw new IllegalArgumentException("offset rotation must not equal (0, 0, 0)");
        }
        int i = 0;
        for (T object : objects) {
            if (object instanceof ParticleCombiner<?> combiner) {
                combiner.setRotationRecursively(
                        rotation.add(offset.x * i, offset.y * i, offset.z * i), offset
                );
            }
            object.setRotation(rotation.add(offset.x * i, offset.y * i, offset.z * i));
            i++;
        }
    }

    /** Sets the particle to use to a new value and returns the previous particle that was used.
     * This applies to all the object(including objects that are way below the hierarchy). The value
     * can also be null meaning that there are different particle effects at play in the object
     * <br><br>
     *
     * @param particle The new particle
     * @return The previous particle
     */
    public ParticleEffect setParticleEffectRecursively(ParticleEffect particle) {
        ParticleEffect prevParticle = super.setParticleEffect(particle);
        for (T object : this.objects) {
            if (object instanceof ParticleCombiner<?> combiner) {
                combiner.setParticleEffectRecursively(particle);
            }
            object.setParticleEffect(particle);
        }
        return prevParticle;
    }

    /** Sets the particle to use to a new value and returns the previous particle that was used.
     * This applies to all the object(including objects that are way below the hierarchy). The value
     * can also be null meaning that there are different particle effects at play in the object.
     * However unlike the {@code setParticleEffectRecursively(ParticleEffect)} which is a list of
     * the particles to use per depth level<br><br>
     *
     * <b>Note:</b> If there is no more particles to supply in the current depth level the system is,
     * it will resort in not going below. Keep that in mind
     * 
     * @param particle The particles per level
     *
     * @see ParticleCombiner#setParticleEffectRecursively(ParticleEffect) 
     */
    public void setParticleEffectRecursively(ParticleEffect[] particle) {
        this.particle = null;
        this.particleEffectRecursiveLogic(particle, 0);
    }
    
    private void particleEffectRecursiveLogic(ParticleEffect[] particle, int depth) {
        for (T object : this.objects) {
            if (depth >= particle.length) break;
            if (object instanceof ParticleCombiner<?> combiner) {
                object.particle = null;
                combiner.particleEffectRecursiveLogic(particle, depth + 1);
            }
            object.setParticleEffect(particle[depth]);
        }
    }

    /** Sets the amount to use to a new value and returns the previous amount that was used.
     * This applies to all the object(including objects that are way below the hierarchy). The returned
     * value can also be -1 meaning that there are different particle effects at play in the object.
     * The offset param changes the amount per object by a specified amount. There is also a simplified version
     * that doesn't recursively scale down the tree & another that allows specifying the recursion offset<br><br>
     *
     * <b>Note:</b> when the program encounters another combiner. It calls the same method but
     * the amount is added with the iterated offset and the offset remains unchanged
     *
     * @param amount The new particle
     * @param offset The offset of the amount(can be positive or negative)
     *
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
     * This applies to all the object(including objects that are way below the hierarchy). The returned
     * value can also be -1 meaning that there are different particle effects at play in the object.
     * The offset param changes the amount per object by a specified amount. There is also a simplified version
     * that doesn't recursively scale down the tree & another that doesn't accept the recursion offset<br><br>
     *
     * <b>Note:</b> when the program encounters another combiner. It calls the same method but
     * the amount is added with the recursive offset and the recursive offset remains unchanged
     *
     * @param amount The new particle
     * @param offset The offset of the amount(can be positive or negative)
     * @param recursiveOffset The offset of the amount once the program encounters another combiner
     *
     * @see ParticleCombiner#setAmountRecursively(int, int)
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

    /** Gets the list of particle objects and returns them
     *
     * @return The list of particle objects
     */
    public T[] getObjects() {return this.objects;}

    /** Gets the particle objects at that index & returns it
     *
     * @param index the index of the particle object
     * @return The list of particle objects
     */
    public T getObject(int index) {return objects[index];}

    @Override
    public void draw(ServerWorld world, int step, Vector3f pos) {
        for (T object : this.objects) {
            InterceptedResult<ParticleCombiner<T>, beforeChildRenderData> modifiedDataBefore =
                    this.interceptRenderChildBefore(world, step, this, object);
            ParticleObject objInUse = (ParticleObject) modifiedDataBefore.interceptData.getMetadata(
                    beforeChildRenderData.OBJECT_IN_USE
            );
            ParticleCombiner<T> comberInUse = modifiedDataBefore.object;
            objInUse.draw(world, step, pos);
            this.interceptRenderChildAfter(world, step, comberInUse);
        }
    }

    private InterceptedResult<ParticleCombiner<T>, emptyData> interceptRenderChildAfter(
            ServerWorld world, int step, ParticleCombiner<T> obj
    ) {
        InterceptData<emptyData> interceptData = new InterceptData<>(
                world, null, step, emptyData.class
        );
        if (this.afterChildRenderIntercept == null) return new InterceptedResult<>(interceptData, obj);
        return this.afterChildRenderIntercept.apply(interceptData, obj);
    }

    private InterceptedResult<ParticleCombiner<T>, beforeChildRenderData> interceptRenderChildBefore(
            ServerWorld world, int step, ParticleCombiner<T> obj, T objectInUse
    ) {
        InterceptData<beforeChildRenderData> interceptData = new InterceptData<>(
                world, null, step, beforeChildRenderData.class
        );
        interceptData.addMetadata(beforeChildRenderData.OBJECT_IN_USE, objectInUse);
        if (this.beforeChildRenderIntercept == null) return new InterceptedResult<>(interceptData, this);
        return this.beforeChildRenderIntercept.apply(interceptData, obj);
    }
}