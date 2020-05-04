package de.flo56958.MineTinker.Modifiers.Types;

import de.flo56958.MineTinker.Data.ToolType;
import de.flo56958.MineTinker.Main;
import de.flo56958.MineTinker.Modifiers.Modifier;
import de.flo56958.MineTinker.Utilities.ConfigurationManager;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class Hardened extends Modifier implements Listener {

	private static Hardened instance;

	private double armorPerLevel;
	private double toughnessPerLevel;

	private Hardened() {
		super(Main.getPlugin());
		customModelData = 10_046;
	}

	public static Hardened instance() {
		synchronized (Hardened.class) {
			if (instance == null) {
				instance = new Hardened();
			}
		}

		return instance;
	}

	@Override
	public String getKey() {
		return "Hardened";
	}

	@Override
	public List<ToolType> getAllowedTools() {
		return Arrays.asList(ToolType.HELMET, ToolType.CHESTPLATE, ToolType.LEGGINGS, ToolType.BOOTS);
	}

	@Override
	public List<Attribute> getAppliedAttributes() {
		return Arrays.asList(Attribute.GENERIC_ARMOR, Attribute.GENERIC_ARMOR_TOUGHNESS);
	}

	@Override
	public boolean applyMod(Player player, ItemStack tool, boolean isCommand) {
		modManager.addArmorAttributes(tool);
		return true;
	}

	public void reapplyAttributes(ItemStack armor) {
		if (!modManager.hasMod(armor, this)) {
			return;
		}

		int level = modManager.getModLevel(armor, this);
		ItemMeta meta = armor.getItemMeta();
		if (meta == null) {
			return;
		}

		{
			Collection<AttributeModifier> attributeModifiers = meta.getAttributeModifiers(Attribute.GENERIC_ARMOR);
			double amount = 0;
			if (!(attributeModifiers == null || attributeModifiers.isEmpty())) {

				for (AttributeModifier attmod : attributeModifiers) {
					amount += attmod.getAmount();
				}
			}
			amount += armorPerLevel * level;

			if (amount > 0) {
				meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
				AttributeModifier armorAM = null;
				if (ToolType.BOOTS.contains(armor.getType())) {
					armorAM = new AttributeModifier(UUID.randomUUID(), "generic.armor", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET);
				} else if (ToolType.CHESTPLATE.contains(armor.getType())) {
					armorAM = new AttributeModifier(UUID.randomUUID(), "generic.armor", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST);
				} else if (ToolType.HELMET.contains(armor.getType())) {
					armorAM = new AttributeModifier(UUID.randomUUID(), "generic.armor", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD);
				} else if (ToolType.LEGGINGS.contains(armor.getType())) {
					armorAM = new AttributeModifier(UUID.randomUUID(), "generic.armor", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS);
				}
				meta.addAttributeModifier(Attribute.GENERIC_ARMOR, armorAM);
			}
		}

		{
			Collection<AttributeModifier> attributeModifiers = meta.getAttributeModifiers(Attribute.GENERIC_ARMOR_TOUGHNESS);
			double amount = 0;
			if (!(attributeModifiers == null || attributeModifiers.isEmpty())) {
				for (AttributeModifier attmod : attributeModifiers) {
					amount += attmod.getAmount();
				}
			}
			amount += toughnessPerLevel * level;
			if (amount > 0) {
				meta.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
				AttributeModifier toughnessAM = null;
				if (ToolType.BOOTS.contains(armor.getType())) {
					toughnessAM = new AttributeModifier(UUID.randomUUID(), "generic.armorToughness", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET);
				} else if (ToolType.CHESTPLATE.contains(armor.getType())) {
					toughnessAM = new AttributeModifier(UUID.randomUUID(), "generic.armorToughness", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST);
				} else if (ToolType.HELMET.contains(armor.getType())) {
					toughnessAM = new AttributeModifier(UUID.randomUUID(), "generic.armorToughness", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD);
				} else if (ToolType.LEGGINGS.contains(armor.getType())) {
					toughnessAM = new AttributeModifier(UUID.randomUUID(), "generic.armorToughness", amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS);
				}
				meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, toughnessAM);
			}
		}
		armor.setItemMeta(meta);
	}

	@Override
	public void reload() {
		FileConfiguration config = getConfig();
		config.options().copyDefaults(true);

		config.addDefault("Allowed", true);
		config.addDefault("Name", "Hardened");
		config.addDefault("ModifierItemName", "Diamond refined Iron");
		config.addDefault("Description", "Harden your armor by %aamount and toughness by %tamount per level!");
		config.addDefault("DescriptionModifierItem", "%WHITE%Modifier-Item for the Hardened-Modifier");
		config.addDefault("Color", "%DARK_GRAY%");
		config.addDefault("MaxLevel", 5);
		config.addDefault("SlotCost", 1);
		config.addDefault("ArmorPerLevel", 1.0);
		config.addDefault("ToughnessPerLevel", 0.5);

		config.addDefault("EnchantCost", 10);
		config.addDefault("Enchantable", false);

		config.addDefault("Recipe.Enabled", true);
		config.addDefault("Recipe.Top", "D D");
		config.addDefault("Recipe.Middle", "III");
		config.addDefault("Recipe.Bottom", "D D");
		config.addDefault("OverrideLanguagesystem", false);

		Map<String, String> recipeMaterials = new HashMap<>();
		recipeMaterials.put("D", Material.DIAMOND.name());
		recipeMaterials.put("I", Material.IRON_BLOCK.name());

		config.addDefault("Recipe.Materials", recipeMaterials);

		ConfigurationManager.saveConfig(config);
		ConfigurationManager.loadConfig("Modifiers" + File.separator, getFileName());

		this.armorPerLevel = config.getDouble("ArmorPerLevel", 1.0);
		this.toughnessPerLevel = config.getDouble("ToughnessPerLevel", 0.5);

		init(Material.IRON_BLOCK, true);

		this.description = this.description.replaceAll("%aamount", String.valueOf(this.armorPerLevel))
				.replaceAll("%tamount", String.valueOf(this.toughnessPerLevel));
	}
}