package cofh.core.network;

import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ItemHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import java.io.*;
import java.util.UUID;

public abstract class PacketCoFHBase extends PacketBase {

	private ByteArrayOutputStream arrayout;
	private DataOutputStream dataout;
	public DataInputStream datain;

	public PacketCoFHBase() {

		arrayout = new ByteArrayOutputStream();
		dataout = new DataOutputStream(arrayout);
	}

	public PacketCoFHBase(byte[] data) {

		datain = new DataInputStream(new ByteArrayInputStream(data));
	}

	public PacketCoFHBase addString(String theString) {

		try {
			dataout.writeUTF(theString);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addUUID(UUID theUUID) {

		try {
			dataout.writeLong(theUUID.getMostSignificantBits());
			dataout.writeLong(theUUID.getLeastSignificantBits());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addLong(long theLong) {

		try {
			dataout.writeLong(theLong);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addInt(int theInteger) {

		try {
			dataout.writeInt(theInteger);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addVarInt(int theInteger) {

		try {
			int v = 0x00;
			if (theInteger < 0) {
				v |= 0x40;
				theInteger = ~theInteger;
			}
			if ((theInteger & ~0x3F) != 0) {
				v |= 0x80;
			}
			dataout.writeByte(v | (theInteger & 0x3F));
			theInteger >>>= 6;
			while (theInteger != 0) {
				dataout.writeByte((theInteger & 0x7F) | ((theInteger & ~0x7F) != 0 ? 0x80 : 0));
				theInteger >>>= 7;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addBool(boolean theBoolean) {

		try {
			dataout.writeBoolean(theBoolean);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addByte(byte theByte) {

		try {
			dataout.writeByte(theByte);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addByte(int theByte) {

		return addByte((byte) theByte);
	}

	public PacketCoFHBase addShort(short theShort) {

		try {
			dataout.writeShort(theShort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addShort(int theShort) {

		return addShort((short) theShort);
	}

	public PacketCoFHBase addByteArray(byte theByteArray[]) {

		try {
			dataout.write(theByteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addFloat(float theFloat) {

		try {
			dataout.writeFloat(theFloat);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addItemStack(ItemStack theStack) {

		try {
			writeItemStack(theStack);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addFluidStack(FluidStack theStack) {

		try {
			FluidHelper.writeFluidStackToPacket(theStack, dataout);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PacketCoFHBase addCoords(TileEntity theTile) {

		addInt(theTile.getPos().getX());
		addInt(theTile.getPos().getY());
		return addInt(theTile.getPos().getZ());
	}

	public PacketCoFHBase addCoords(int x, int y, int z) {

		addInt(x);
		addInt(y);
		return addInt(z);
	}

	public String getString() {

		try {
			return datain.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public UUID getUUID() {

		try {
			long msb = datain.readLong();
			long lsb = datain.readLong();
			return new UUID(msb, lsb);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public long getLong() {

		try {
			return datain.readLong();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getInt() {

		try {
			return datain.readInt();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int getVarInt() {

		try {
			int v = datain.readByte(), r = v & 0x3F;
			boolean n = (v & 0x40) != 0;
			for (int i = 6; (v & 0x80) != 0; i += 7) {
				v = datain.readByte();
				r |= (v & 0x7F) << i;
			}
			return n ? ~r : r;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public boolean getBool() {

		try {
			return datain.readBoolean();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public byte getByte() {

		try {
			return datain.readByte();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public short getShort() {

		try {
			return datain.readShort();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void getByteArray(byte theByteArray[]) {

		try {
			datain.readFully(theByteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float getFloat() {

		try {
			return datain.readFloat();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public ItemStack getItemStack() {

		try {
			return readItemStack();
		} catch (IOException e) {
			e.printStackTrace();
			return ItemStack.EMPTY;
		}
	}

	public FluidStack getFluidStack() {

		try {
			return FluidHelper.readFluidStackFromPacket(datain);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public BlockPos getCoords() {

		return new BlockPos(getInt(), getInt(), getInt());
	}

	private void writeItemStack(ItemStack theStack) throws IOException {

		if (theStack.isEmpty()) {
			addShort(-1);
		} else {
			addShort(Item.getIdFromItem(theStack.getItem()));
			addByte(theStack.getCount());
			addShort(ItemHelper.getItemDamage(theStack));
			writeNBT(theStack.getTagCompound());
		}
	}

	public ItemStack readItemStack() throws IOException {

		ItemStack stack = ItemStack.EMPTY;
		short itemID = getShort();

		if (itemID >= 0) {
			byte stackSize = getByte();
			short damage = getShort();
			stack = new ItemStack(Item.getItemById(itemID), stackSize, damage);
			stack.setTagCompound(readNBT());
		}

		return stack;
	}

	public void writeNBT(NBTTagCompound nbt) throws IOException {

		if (nbt == null) {
			addShort(-1);
		} else {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			CompressedStreamTools.writeCompressed(nbt, byteArrayOutputStream);
			byte[] abyte = byteArrayOutputStream.toByteArray();
			addShort((short) abyte.length);
			addByteArray(abyte);
		}
	}

	public NBTTagCompound readNBT() throws IOException {

		short nbtLength = getShort();

		if (nbtLength < 0) {
			return null;
		} else {
			byte[] abyte = new byte[nbtLength];
			getByteArray(abyte);
			return CompressedStreamTools.readCompressed(new ByteArrayInputStream(abyte));
		}
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {

		buffer.writeBytes(arrayout.toByteArray());
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {

		datain = new DataInputStream(new ByteArrayInputStream(buffer.array()));
		try {
			datain.skipBytes(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player) {

		if (player == null) {
			return;
		}
		handlePacket(player, false);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {

		if (player == null) {
			return;
		}
		handlePacket(player, true);
	}

	public abstract void handlePacket(EntityPlayer player, boolean isServer);

}
