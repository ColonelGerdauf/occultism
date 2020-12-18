/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.klikli_dev.occultism.common.entity.spirit;

import com.github.klikli_dev.occultism.Occultism;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Optional;

public class WildHuntSkeletonEntity extends SkeletonEntity {
    //region Fields
    public static final ResourceLocation wildHuntTag = new ResourceLocation(Occultism.MODID, "wild_hunt");
    protected Optional<WildHuntWitherSkeletonEntity> master = Optional.empty();
    //endregion Fields

    //region Initialization
    public WildHuntSkeletonEntity(EntityType<? extends WildHuntSkeletonEntity> type,
                                  World worldIn) {
        super(type, worldIn);
    }
    //endregion Initialization

    //region Getter / Setter
    public void setMaster(WildHuntWitherSkeletonEntity master) {
        this.master = Optional.ofNullable(master);
    }
    //endregion Getter / Setter

    //region Overrides
    @Override
    protected boolean isDespawnPeaceful() {
        return false;
    }

    @Override
    protected boolean isInDaylight() {
        return false;
    }

    @Override
    public void remove(boolean keepData) {
        this.master.ifPresent(boss -> {
            boss.notifyMinionDeath(this);
        });
        super.remove(keepData);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        ITag<EntityType<?>> wildHuntTags = EntityTypeTags.getCollection().get(wildHuntTag);

        Entity trueSource = source.getTrueSource();
        if (trueSource != null && wildHuntTags.contains(trueSource.getType()))
            return true;

        Entity immediateSource = source.getImmediateSource();
        if (immediateSource != null && wildHuntTags.contains(immediateSource.getType()))
            return true;

        return super.isInvulnerableTo(source);
    }
    //endregion Overrides

    //region Static Methods
    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return SkeletonEntity.registerAttributes()
                       .createMutableAttribute(Attributes.ATTACK_DAMAGE, 4.0)
                       .createMutableAttribute(Attributes.MAX_HEALTH, 20.0);
    }
    //endregion Static Methods
}
