/*
 * This file is part of MetaShop, licensed under the ISC License.
 *
 * Copyright (c) 2015 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package com.tealcube.minecraft.spigot.metashop.commands;

import com.tealcube.minecraft.bukkit.hilt.HiltItemStack;
import com.tealcube.minecraft.spigot.metashop.MetaShopPlugin;
import com.tealcube.minecraft.spigot.metashop.managers.SessionManager;
import com.tealcube.minecraft.spigot.metashop.managers.ShopManager;
import com.tealcube.minecraft.spigot.metashop.sessions.ShopEditSession;
import com.tealcube.minecraft.spigot.metashop.shops.Shop;
import com.tealcube.minecraft.spigot.metashop.shops.ShopItem;
import com.tealcube.minecraft.spigot.metashop.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.ranzdo.bukkit.methodcommand.Arg;
import se.ranzdo.bukkit.methodcommand.Command;

public class MetaShopCommand {

    private final MetaShopPlugin plugin;

    public MetaShopCommand(MetaShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(identifier = "metashop show", permissions = "metashop.command.show", onlyPlayers = false)
    public void showSubcommand(CommandSender sender, @Arg(name = "shop") String shopName, @Arg(name = "target", def = "?sender") Player target) {
        Shop shop = ShopManager.getShop(shopName);
        if (shop == null) {
            MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.unable-to-open-sender"));
            if (!sender.equals(target)) {
                MessageUtils.sendMessage(target, plugin.getSettings().getString("language.unable-to-open-receiver"));
            }
            return;
        }
        shop.open(target);
        MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.successful-open"));
    }

    @Command(identifier = "metashop list", permissions = "metashop.command.list", onlyPlayers = false)
    public void listSubcommand(CommandSender sender) {
        MessageUtils.sendMessage(sender, "<white>Use the first name given below.");
        for (Shop shop : ShopManager.getShops()) {
            MessageUtils.sendMessage(sender, shop.getId() + " : " + shop.getName());
        }
    }

    @Command(identifier = "metashop select", permissions = "metashop.command.select", onlyPlayers = true)
    public void selectSubcommand(Player sender, @Arg(name = "shop name") String shopName) {
        Shop shop = ShopManager.getShop(shopName);
        if (shop == null) {
            MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.unable-to-select-shop"));
            return;
        }
        ShopEditSession shopEditSession = new ShopEditSession(sender.getUniqueId());
        shopEditSession.setShopId(shop.getId());
        SessionManager.addShopEditSession(shopEditSession);
        MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.successful-shop-selection"));
    }

    @Command(identifier = "metashop add", permissions = "metashop.command.add", onlyPlayers = true)
    public void addSubcommand(Player sender, @Arg(name = "index") int index, @Arg(name = "price") double price) {
        if (!SessionManager.hasShopEditSession(sender.getUniqueId())) {
            MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.no-session"));
            return;
        }
        ShopEditSession session = SessionManager.getShopEditSession(sender.getUniqueId());
        if (session.getShopId() == null) {
            MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.no-session"));
            return;
        }
        if (sender.getItemInHand() == null || sender.getItemInHand().getType() == Material.AIR) {
            MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.unsupported-item"));
            return;
        }
        HiltItemStack his = new HiltItemStack(sender.getItemInHand());
        Shop shop = ShopManager.getShop(session.getShopId());
        if (shop == null) {
            MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.shop-does-not-exist"));
            return;
        }
        ShopItem item = new ShopItem(his, price);
        shop.setItem(index, item);
        MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.successful-add-item"));
    }

    @Command(identifier = "metashop remove", permissions = "metashop.command.remove", onlyPlayers = true)
    public void removeSubcommand(Player sender, @Arg(name = "index") int index) {
        if (!SessionManager.hasShopEditSession(sender.getUniqueId())) {
            MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.no-session"));
            return;
        }
        ShopEditSession session = SessionManager.getShopEditSession(sender.getUniqueId());
        if (session.getShopId() == null) {
            MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.no-session"));
            return;
        }
        Shop shop = ShopManager.getShop(session.getShopId());
        if (shop == null) {
            MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.shop-does-not-exist"));
            return;
        }
        shop.setItem(index, null);
        MessageUtils.sendMessage(sender, plugin.getSettings().getString("language.successful-remove-item"));
    }

}
