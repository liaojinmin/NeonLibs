package me.neon.libs.taboolib.ui

import org.bukkit.inventory.Inventory

abstract class Menu(var title: String) {

    abstract fun build(): Inventory
}