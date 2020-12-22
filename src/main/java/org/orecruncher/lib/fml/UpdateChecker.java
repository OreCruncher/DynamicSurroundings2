/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.lib.fml;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.forgespi.language.IModInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class UpdateChecker {

    private final String modId;
    private final String messageId;

    private UpdateChecker(@Nonnull final String id, @Nonnull final String messageId) {
        this.modId = id;
        this.messageId = messageId;
    }

    private static boolean shouldPrintMessage(@Nonnull final CheckResult result) {
        return result.status == Status.OUTDATED;
    }

    /**
     * Checks the Forge update state for the specified mod, and report the need of an update to the
     * chat output of Minecraft.
     *
     * @param event The PlayerLoggedInEvent
     * @param modId Id of the mod in question
     */
    public static void doCheck(@Nonnull final ClientPlayerNetworkEvent.LoggedInEvent event, @Nonnull String modId) {
        doCheck(event, modId, modId + ".msg.NewVersion");
    }

    /**
     * Checks the Forge update state for the specified mod, and report the need of an update to the
     * chat output of Minecraft.
     *
     * @param event The PlayerLoggedInEvent
     * @param modId Id of the mod in question
     * @param msgId Resource string of the update message to display in chat
     */
    public static void doCheck(@Nonnull final ClientPlayerNetworkEvent.LoggedInEvent event, @Nonnull String modId, @Nonnull String msgId) {
        if (event.getPlayer() != null) {
            new UpdateChecker(modId, msgId).playerLogin(event);
        }
    }

    @Nullable
    private String getUpdateMessage(@Nonnull final String modId) {
        final Optional<IModInfo> mod = ForgeUtils.getModInfo(modId);
        if (!mod.isPresent())
            return null;
        final CheckResult result = VersionChecker.getResult(mod.get());
        if (!shouldPrintMessage(result))
            return null;
        final String t = result.target != null ? result.target.toString() : "UNKNOWN";
        final String u = result.url != null ? result.url : "UNKNOWN";
        return I18n.format(this.messageId, mod.get().getDisplayName(), t, u);
    }

    private void playerLogin(@Nonnull final ClientPlayerNetworkEvent.LoggedInEvent event) {
        if (event.getPlayer() != null) {
            final String updateMessage = getUpdateMessage(this.modId);
            if (updateMessage != null) {
                try {
                    final ITextComponent component = ITextComponent.Serializer.getComponentFromJson(updateMessage);
                    if (component != null)
                        event.getPlayer().sendMessage(component, UUID.randomUUID());
                } catch (@Nonnull final Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
