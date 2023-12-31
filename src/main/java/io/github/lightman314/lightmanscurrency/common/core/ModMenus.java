package io.github.lightman314.lightmanscurrency.common.core;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.*;
import io.github.lightman314.lightmanscurrency.common.menu.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.common.menu.wallet.WalletMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModMenus {

    //ATM
    public static final ScreenHandlerType<ATMMenu> ATM = new ScreenHandlerType<>(ATMMenu::new, FeatureSet.empty());
    //Coin Mint
    public static final ScreenHandlerType<MintMenu> MINT = new ExtendedScreenHandlerType<>((windowID, inventory, buffer) -> new MintMenu(windowID, inventory, buffer.readBlockPos()));

    //Trader
    public static final ScreenHandlerType<TraderMenu> TRADER = new ExtendedScreenHandlerType<>((windowID, inventory, buffer) -> new TraderMenu(windowID, inventory, buffer.readLong()));
    public static final ScreenHandlerType<TraderMenu.TraderMenuBlockSource> TRADER_BLOCK = new ExtendedScreenHandlerType<>((windowID, inventory, buffer) -> new TraderMenu.TraderMenuBlockSource(windowID, inventory, buffer.readBlockPos()));
    public static final ScreenHandlerType<TraderMenu.TraderMenuAllNetwork> TRADER_NETWORK_ALL = new ScreenHandlerType<>(TraderMenu.TraderMenuAllNetwork::new, FeatureSet.empty());
    public static final ScreenHandlerType<SlotMachineMenu> SLOT_MACHINE = new ExtendedScreenHandlerType<>((windowID, inventory, buffer) -> new SlotMachineMenu(windowID, inventory, buffer.readLong()));

    //Trader Storage
    public static final ScreenHandlerType<TraderStorageMenu> TRADER_STORAGE = new ExtendedScreenHandlerType<>((windowID, inventory, buffer) -> new TraderStorageMenu(windowID, inventory, buffer.readLong()));

    //Wallet
    public static final ScreenHandlerType<WalletMenu> WALLET = new ExtendedScreenHandlerType<>((windowID, inventory, buffer) -> new WalletMenu(windowID, inventory, buffer.readInt()));
    public static final ScreenHandlerType<WalletBankMenu> WALLET_BANK = new ExtendedScreenHandlerType<>((windowID, inventory, buffer) -> new WalletBankMenu(windowID, inventory, buffer.readInt()));

    //Ticket Machine
    public static final ScreenHandlerType<TicketMachineMenu> TICKET_MACHINE = new ExtendedScreenHandlerType<>((windowID, inventory, buffer) -> new TicketMachineMenu(windowID, inventory, buffer.readBlockPos()));

    //Trader Interface
    public static final ScreenHandlerType<TraderInterfaceMenu> TRADER_INTERFACE = new ExtendedScreenHandlerType<>((windowID, inventory, buffer) -> new TraderInterfaceMenu(windowID, inventory, buffer.readBlockPos()));

    //Trader Recover
    public static final ScreenHandlerType<TraderRecoveryMenu> TRADER_RECOVERY = new ScreenHandlerType<>(TraderRecoveryMenu::new, FeatureSet.empty());

    public static void registerMenus()
    {
        //ATM
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "atm"), ATM);
        //Coin Mint
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "coinmint"), MINT);

        //Trader
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "trader"), TRADER);
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "trader_block"), TRADER_BLOCK);
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "trader_network_all"), TRADER_NETWORK_ALL);
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "slot_machine"), SLOT_MACHINE);

        //Trader Storage
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "trader_storage"), TRADER_STORAGE);

        //Wallet
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "wallet"), WALLET);
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "wallet_bank"), WALLET_BANK);

        //Ticket Machine
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "ticket_machine"), TICKET_MACHINE);

        //Trader Interface
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "trader_interface"), TRADER_INTERFACE);

        //Trader Recovery
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(LightmansCurrency.MODID, "trader_recovery"), TRADER_RECOVERY);

    }

}