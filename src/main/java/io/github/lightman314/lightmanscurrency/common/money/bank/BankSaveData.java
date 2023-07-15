package io.github.lightman314.lightmanscurrency.common.money.bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.data.ClientBankData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageSetupClientBank;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageSyncSelectedBankAccount;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageUpdateClientBank;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.CMessageSelectBankAccount;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class BankSaveData extends PersistentState {


    private final Map<UUID, Pair<BankAccount, BankAccount.AccountReference>> playerBankData = new HashMap<>();

    private BankSaveData() {}
    private BankSaveData(NbtCompound compound) {

        NbtList bankData = compound.getList("PlayerBankData", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < bankData.size(); ++i)
        {
            NbtCompound tag = bankData.getCompound(i);
            UUID player = tag.getUuid("Player");
            BankAccount bankAccount = loadBankAccount(player, tag.getCompound("BankAccount"));
            BankAccount.AccountReference lastSelected = BankAccount.LoadReference(false, tag.getCompound("LastSelected"));
            playerBankData.put(player, Pair.of(bankAccount, lastSelected));
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {

        NbtList bankData = new NbtList();
        this.playerBankData.forEach((player,data) -> {
            NbtCompound tag = new NbtCompound();
            tag.putUuid("Player", player);
            tag.put("BankAccount", data.getFirst().save());
            tag.put("LastSelected", data.getSecond().save());
            bankData.add(tag);
        });
        compound.put("PlayerBankData", bankData);

        return compound;
    }

    private static BankAccount loadBankAccount(UUID player, NbtCompound compound) {
        BankAccount bankAccount = new BankAccount(() -> MarkBankAccountDirty(player), compound);
        try {
            bankAccount.setNotificationConsumer(BankAccount.generateNotificationAcceptor(player));
            bankAccount.updateOwnersName(PlayerReference.of(player, bankAccount.getOwnersName()).getName(false));
        } catch(Throwable t) { t.printStackTrace(); }
        return bankAccount;
    }

    private static BankAccount generateBankAccount(UUID player) {
        BankAccount bankAccount = new BankAccount(() -> MarkBankAccountDirty(player));
        try {
            bankAccount.setNotificationConsumer(BankAccount.generateNotificationAcceptor(player));
            bankAccount.updateOwnersName(PlayerReference.of(player, bankAccount.getOwnersName()).getName(false));
        } catch(Throwable t) { t.printStackTrace(); }
        return bankAccount;
    }

    private static BankSaveData get() {
        MinecraftServer server = ServerHook.getServer();
        if(server != null)
        {
            ServerWorld level = server.getOverworld();
            if(level != null)
                return level.getPersistentStateManager().getOrCreate(BankSaveData::new, BankSaveData::new, "lightmanscurrency_bank_data");
        }
        return null;
    }

    public static List<BankAccount.AccountReference> GetPlayerBankAccounts() {
        List<BankAccount.AccountReference> results = new ArrayList<>();
        BankSaveData bsd = get();
        if(bsd != null)
            bsd.playerBankData.forEach((player,data) -> results.add(BankAccount.GenerateReference(false, player)));
        return results;
    }

    public static BankAccount GetBankAccount(PlayerEntity player) { return GetBankAccount(player.getWorld().isClient, player.getUuid()); }

    public static BankAccount GetBankAccount(boolean isClient, UUID player) {
        if(player == null)
            return null;
        if(isClient)
        {
            return ClientBankData.GetPlayerBankAccount(player);
        }
        else
        {
            BankSaveData bsd = get();
            if(bsd != null)
            {
                if(bsd.playerBankData.containsKey(player))
                    return bsd.playerBankData.get(player).getFirst();
                //Create a new bank account for the player
                BankAccount newAccount = generateBankAccount(player);
                bsd.playerBankData.put(player, Pair.of(newAccount, BankAccount.GenerateReference(false, player)));
                MarkBankAccountDirty(player);
                return newAccount;
            }
            return null;
        }
    }

    @Deprecated /** @deprecated Only use to transfer bank account data from the old Trading Office. */
    public static void GiveOldBankAccount(UUID player, BankAccount account) {
        BankSaveData bsd = get();
        if(bsd != null)
        {
            if(bsd.playerBankData.containsKey(player))
                bsd.playerBankData.put(player, Pair.of(account, bsd.playerBankData.get(player).getSecond()));
            else
                bsd.playerBankData.put(player, Pair.of(account, BankAccount.GenerateReference(false, player)));
        }
    }

    public static void MarkBankAccountDirty(UUID player)
    {
        BankSaveData bsd = get();
        if(bsd != null)
        {
            bsd.markDirty();
            //Send update packet to all connected clients
            BankAccount bankAccount = GetBankAccount(false, player);
            NbtCompound compound = bankAccount.save();
            compound.putUuid("Player", player);
            new SMessageUpdateClientBank(compound).sendToAll();
        }
    }

    public static BankAccount.AccountReference GetSelectedBankAccount(PlayerEntity player) {
        if(player.getWorld().isClient)
        {
            ClientBankData.GetLastSelectedAccount();
        }
        else
        {
            BankSaveData bsd = get();
            if(bsd != null)
            {
                if(bsd.playerBankData.containsKey(player.getUuid()))
                {
                    BankAccount.AccountReference account = bsd.playerBankData.get(player.getUuid()).getSecond();
                    if(!account.allowedAccess(player))
                    {
                        LightmansCurrency.LogInfo(player.getName().getString() + " is no longer allowed to access their selected bank account. Switching back to their personal account.");
                        account = BankAccount.GenerateReference(player);
                        SetSelectedBankAccount(player, account);
                    }
                    return account;
                }
                //Generate default bank account for the player
                BankAccount.AccountReference account = BankAccount.GenerateReference(player);
                SetSelectedBankAccount(player,account);
                return account;
            }
        }
        return BankAccount.GenerateReference(player);
    }

    public static void SetSelectedBankAccount(PlayerEntity player, BankAccount.AccountReference account) {
        //Ignore if the account is null or the player isn't allowed to access it.
        if(account == null)
            return;
        if(player.getWorld().isClient)
        {
            new CMessageSelectBankAccount(account).sendToServer();
        }
        else
        {
            if(!account.allowedAccess(player))
            {
                LightmansCurrency.LogDebug("Player does not have access to the selected account. Canceling selection.");
                return;
            }
            BankSaveData bsd = get();
            if(bsd != null)
            {
                if(bsd.playerBankData.containsKey(player.getUuid()))
                {
                    bsd.playerBankData.put(player.getUuid(), Pair.of(bsd.playerBankData.get(player.getUuid()).getFirst(), account));
                }
                else
                {
                    bsd.playerBankData.put(player.getUuid(), Pair.of(generateBankAccount(player.getUuid()),account));
                    MarkBankAccountDirty(player.getUuid());
                }

                bsd.markDirty();
                new SMessageSyncSelectedBankAccount(account).sendTo(player);
            }
        }
    }

    public static void OnPlayerLogin(ServerPlayerEntity player, PacketSender sender)
    {

        BankSaveData bsd = get();

        if(bsd != null)
        {
            //Confirm the presence of the loading players bank account
            GetBankAccount(player);

            NbtCompound compound = new NbtCompound();
            NbtList bankList = new NbtList();
            bsd.playerBankData.forEach((id, data) -> {
                NbtCompound tag = data.getFirst().save();
                tag.putUuid("Player", id);
                bankList.add(tag);
            });
            compound.put("BankAccounts", bankList);
            new SMessageSetupClientBank(compound).sendTo(sender);

            //Update to let them know their selected bank account
            BankAccount.AccountReference selectedAccount = GetSelectedBankAccount(player);
            new SMessageSyncSelectedBankAccount(selectedAccount).sendTo(sender);
        }

    }

}