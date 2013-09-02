package owen_io
import com.sun.jna.Native,
       com.sun.jna.Library,
       com.sun.jna.ptr.{FloatByReference,IntByReference,ShortByReference,ByteByReference},
       java.net.URLDecoder,
       com.sun.jna.Pointer

       
class OwenIO extends Library{
  //Buffers
  val bCommand =  Array.fill[Byte](32)(0)
  val text = Array.fill[Byte](512)(0)
  val rInt = new IntByReference(0) 
  val rFloat = new FloatByReference(0f) 
  val rShort = new ShortByReference(0)
  val rByte = new ByteByReference(0)
  //Load owen lib
  private val url = getClass().getResource("OwenIO.class")
  private val pa = URLDecoder.decode(url.getPath(), "utf-8").split("/")
  private val pf = pa.dropRight(3).drop(1).mkString("/") + "/lib/owen/owen_io.dll"
  System.load(pf)
  private val dll = Native.loadLibrary("owen_io", classOf[owen_io]).asInstanceOf[owen_io]
  //Methods
  def setApiMode(mode:Int) = {
    ifError(dll.SetApiMode(mode))
  }
  def openPort(port:Port) = {
    ifError(dll.OpenPort(port.num, port.speed, port.parity, port.bits, port.stop, port.converter))
  }
  def closePort() = {
    ifError(dll.ClosePort())
  }
  def selectPort(n:Int) = {
    ifError(dll.SelectPort(n))
  }
  def setupPort(port:Port) = {
    ifError(dll.SetupPort(port.num, port.speed, port.parity, port.bits, port.stop, port.converter))
  }
  def lastErrToStr():String = {
    ifError(dll.LastErrToStr(text)); Native.toString(text, "Cp1251")
  }
  def getExtendedLastErr():Int = {
    dll.GetExtendedLastErr
  }
  def setDbgIndication(i:Int) = {
    ifError(dll.SetDbgIndication(i))
  }
  def getMaxRetriesGlobal():Int = {
    dll.GetMaxRetriesGlobal
  }
  def setMaxRetriesGlobal(NumberOfRetries:Int) = {
    ifError(dll.SetMaxRetriesGlobal(NumberOfRetries))
  }
  def readSInt(adr:Address, command:Command):Int = {
    buildCommand(command.command)
    ifError(dll.ReadSInt(adr.adr, adr.adr_type, bCommand, rInt, command.index)) 
    rInt.getValue()
  }
  def readUInt(adr:Address, command:Command):Int = {
    buildCommand(command.command)
    ifError(dll.ReadUInt(adr.adr, adr.adr_type, bCommand, rInt, command.index))
    rInt.getValue()
  }
  def writeByte(adr:Address, command:Command, value:Int) = {
    buildCommand(command.command)
    val pb = Array.fill[Byte](512)(0); val ca = command.command.toCharArray()
    ifError(dll.WriteByte(adr.adr, adr.adr_type, bCommand, value, command.index))  
  }
  def writeWord(adr:Address, command:Command, value:Int) = {
    buildCommand(command.command)
    ifError(dll.WriteWord(adr.adr, adr.adr_type, bCommand, value, command.index))
  }
  def readFloat24(adr:Address, command:Command):Float = {
    buildCommand(command.command)
    ifError(dll.ReadFloat24(adr.adr, adr.adr_type, bCommand, rFloat, command.index)); 
    rFloat.getValue() 
  }
  def readStoredDotS(adr:Address, command:Command):Float = {
    buildCommand(command.command)
    ifError(dll.ReadStoredDotS(adr.adr, adr.adr_type, bCommand, rFloat, command.index))  
    rFloat.getValue()  
  }
  def readIEEE32(adr:Address, command:Command):(Float,Int) = {
    buildCommand(command.command)
    ifError(dll.ReadIEEE32(adr.adr, adr.adr_type, bCommand, rFloat, rInt, command.index))
    (rFloat.getValue(),rInt.getValue())
  }
  def writeFloat24(adr:Address, command:Command, value:Float) = {
    buildCommand(command.command)
    ifError(dll.WriteFloat24(adr.adr, adr.adr_type, bCommand, value, command.index))
  }
  def writeStoredDotS(adr:Address, command:Command, value:Float) = {
    buildCommand(command.command)
    ifError(dll.WriteStoredDotS(adr.adr, adr.adr_type, bCommand, value, command.index))
  }
  def writeIEEE32(adr:Address, command:Command, value:Float) = {
    buildCommand(command.command)
    ifError(dll.WriteIEEE32(adr.adr, adr.adr_type, bCommand, value, command.index))
  }
  def owenIO(adr:Address, is_read:Int, command:String, params:String, param_sz:Int):(String, Int) = {
    buildCommand(command)
    buildText(params)
    ifError(dll.OwenIO(adr.adr, adr.adr_type, is_read, bCommand, text, rInt))
    (Native.toString(text, "Cp1251"),rInt.getValue())
  }
  def readDTMR(adr:Address):Time = {
    val hrs = new IntByReference(0); val mins = new IntByReference(0); val sec = new IntByReference(0); val msec = new IntByReference(0) 
    ifError(dll.ReadDTMR(adr.adr, adr.adr_type, hrs, mins, sec, msec))
    new Time(hrs.getValue(), mins.getValue(), sec.getValue(), msec.getValue())
  }
  def readSI8BCD(adr:Address, command:String):Int = {
    buildCommand(command)
    ifError(dll.ReadSI8BCD(adr.adr, adr.adr_type, bCommand, rInt))
    rInt.getValue()
  }
  def writeCSET(adr:Address, prc:Int) = {
    ifError(dll.WriteCSET(adr.adr, adr.adr_type, prc))
  }
  def readPKPBCD(adr:Address, command:String):Int = {
    buildCommand(command)
    ifError(dll.ReadPKPBCD(adr.adr, adr.adr_type, bCommand, rInt)) 
    rInt.getValue() 
  }
  def readRTC(adr:Address):String = {
    val a = Array.fill[Byte](32)(0); ifError(dll.ReadRTC(adr.adr, adr.adr_type, a)); Native.toString(a, "Cp1251")
  }
  def listen(timeout:Int):(Short, Int, Int, Array[Byte], Int) = {
    val rcv_hash = new ShortByReference(0); val rcv_flag = new IntByReference(0); val rcv_adr = new IntByReference(0); val param_sz = new IntByReference(0)
    val param_data = Array.fill[Byte](32)(0)
    ifError(dll.Listen(timeout, rcv_hash, rcv_flag, rcv_adr, param_data, param_sz))
    (rcv_hash.getValue(),rcv_flag.getValue(),rcv_adr.getValue(),param_data,param_sz.getValue())
  }
  def getBufferSInt(buffer:Buffer):Int = {
    ifError(dll.GetBufferSInt(buffer.buffer, buffer.buffer_size, rInt)) 
    rInt.getValue() 
  }
  def getBufferUInt(buffer:Buffer):Int = {
    ifError(dll.GetBufferUInt(buffer.buffer, buffer.buffer_size, rInt))
    rInt.getValue() 
  }
  def getBufferByte(buffer:Buffer):Byte = {
    ifError(dll.GetBufferByte(buffer.buffer, buffer.buffer_size, rByte))
    rByte.getValue() 
  }
  def getBufferWord(buffer:Buffer):Short = {
    ifError(dll.GetBufferWord(buffer.buffer, buffer.buffer_size, rShort))
    rShort.getValue() 
  }
  def getBufferFloat24(buffer:Buffer):Float = {
    ifError(dll.GetBufferFloat24(buffer.buffer, buffer.buffer_size, rFloat))
    rFloat.getValue() 
  }
  def getBufferStoredDotS(buffer:Buffer):Float = {
    ifError(dll.GetBufferStoredDotS(buffer.buffer, buffer.buffer_size, rFloat)) 
    rFloat.getValue()
  }
  def getBufferStoredDotU(buffer:Buffer):Float = {
    ifError(dll.GetBufferStoredDotU(buffer.buffer, buffer.buffer_size, rFloat)) 
    rFloat.getValue()
  }
  def getBufferStoredIEEE32(buffer:Buffer):Float = {
    ifError(dll.GetBufferStoredIEEE32(buffer.buffer, buffer.buffer_size, rFloat)) 
    rFloat.getValue()
  }
  def getBufferDTMR(buffer:Buffer):Time = {
    val hrs = new IntByReference(0); val mins = new IntByReference(0); val sec = new IntByReference(0); val msec = new IntByReference(0) 
    ifError(dll.GetBufferDTMR(buffer.buffer, buffer.buffer_size, hrs, mins, sec, msec))
    new Time(hrs.getValue(), mins.getValue(), sec.getValue(), msec.getValue())
  }
  def getBufferSI8BCD(buffer:Buffer):Int = {
    ifError(dll.GetBufferSI8BCD(buffer.buffer, buffer.buffer_size, rInt))
    rInt.getValue()
  }
  def getBufferPkpBCD(buffer:Buffer):Float = {
    ifError(dll.GetBufferPkpBCD(buffer.buffer, buffer.buffer_size, rFloat))
    rFloat.getValue()
  }
  def getBufferRTC(buffer:Buffer):String = {
    ifError(dll.GetBufferRTC(buffer.buffer, buffer.buffer_size, text))
    Native.toString(text, "Cp1251")
  }
  def ac2_Open(n:Int) = {
    ifError(dll.AC2_Open(n))
  }
  def ac2_Close() = {
    ifError(dll.AC2_Close())
  }
  def ac2_ReadMpr51(ch:Int, speed:Int):Mpr51 = {
    val t_prod = new FloatByReference(0f); val t_suhogo = new FloatByReference(0f) 
    val t_vlag = new FloatByReference(0f); val otn_vlag = new FloatByReference(0f)  
    ifError(dll.AC2_ReadMpr51(ch, speed, t_prod, t_suhogo, t_vlag, otn_vlag))
    new Mpr51(t_prod.getValue(), t_suhogo.getValue(), t_vlag.getValue(), otn_vlag.getValue())
  }
  def ac2_ReadTRM__PiC(ch:Int):TRM = {
    val Temperature = new FloatByReference(0f); val Ust1 = new FloatByReference(0f) 
    val Ust2 = new FloatByReference(0f); val Delta1 = new FloatByReference(0f)  
    val Delta2 = new FloatByReference(0f) 
    val Rele1 = new IntByReference(0); val Rele2 = new IntByReference(0)
    ifError(dll.AC2_ReadTRM__PiC(ch, Temperature, Rele1, Rele2, Ust1, Ust2, Delta1, Delta2))
    new TRM(Temperature.getValue(), Rele1.getValue(), Rele2.getValue(), Ust1.getValue(), Ust2.getValue(), Delta1.getValue(), Delta2.getValue())
  }
  def ac2_WriteTRM__PiC(ch:Int, trm:TRM) = {
    val Temperature = new FloatByReference(trm.Temperature); val Ust1 = new FloatByReference(trm.Ust1) 
    val Ust2 = new FloatByReference(trm.Ust2); val Delta1 = new FloatByReference(trm.Delta1)  
    val Delta2 = new FloatByReference(trm.Delta2) 
    val Rele1 = new IntByReference(trm.Rele1); val Rele2 = new IntByReference(trm.Rele2)  
    ifError(dll.AC2_WriteTRM__PiC(ch, Temperature, Rele1, Rele2, Ust1, Ust2, Delta1, Delta2))
  }
  def ac2_ReadTRM_UKT_38_T_and_U(ch:Int):TRMUKT38 = {
    val t1 = new FloatByReference(0f); val u1 = new FloatByReference(0f)
    val t2 = new FloatByReference(0f); val u2 = new FloatByReference(0f)
    val t3 = new FloatByReference(0f); val u3 = new FloatByReference(0f)
    val t4 = new FloatByReference(0f); val u4 = new FloatByReference(0f)
    val t5 = new FloatByReference(0f); val u5 = new FloatByReference(0f)
    val t6 = new FloatByReference(0f); val u6 = new FloatByReference(0f)
    val t7 = new FloatByReference(0f); val u7 = new FloatByReference(0f)
    val t8 = new FloatByReference(0f); val u8 = new FloatByReference(0f)
    ifError(dll.AC2_ReadTRM_UKT_38_T_and_U(ch,t1, t2, t3, t4, t5, t6, t7, t8, u1, u2, u3, u4, u5, u6,  u7, u8))
    new TRMUKT38(t1.getValue(), t2.getValue(), t3.getValue(), t4.getValue(), t5.getValue(), t6.getValue(), t7.getValue(), t8.getValue(), 
      u1.getValue(), u2.getValue(), u3.getValue(), u4.getValue(), u5.getValue(), u6.getValue(), u7.getValue(), u8.getValue())  
  }
  def ac2_ReadUKT38sh4_IU(ch:Int, speed:Int):UKT38 = {
    val t1 = new FloatByReference(0f); val u1 = new FloatByReference(0f); val d1 = new FloatByReference(0f)
    val t2 = new FloatByReference(0f); val u2 = new FloatByReference(0f); val d2 = new FloatByReference(0f)
    val t3 = new FloatByReference(0f); val u3 = new FloatByReference(0f); val d3 = new FloatByReference(0f)
    val t4 = new FloatByReference(0f); val u4 = new FloatByReference(0f); val d4 = new FloatByReference(0f)
    val t5 = new FloatByReference(0f); val u5 = new FloatByReference(0f); val d5 = new FloatByReference(0f)
    val t6 = new FloatByReference(0f); val u6 = new FloatByReference(0f); val d6 = new FloatByReference(0f)
    val t7 = new FloatByReference(0f); val u7 = new FloatByReference(0f); val d7 = new FloatByReference(0f)
    val t8 = new FloatByReference(0f); val u8 = new FloatByReference(0f); val d8 = new FloatByReference(0f)
    ifError(dll.AC2_ReadUKT38sh4_IU(ch, speed, t1, t2, t3, t4, t5, t6, t7, t8, u1, u2, u3, u4, u5, u6, u7, u8, d1, d2, d3, d4, d5, d6, d7, d8))
    new UKT38(t1.getValue(), t2.getValue(), t3.getValue(), t4.getValue(), t5.getValue(), t6.getValue(), t7.getValue(), t8.getValue(), 
      u1.getValue(), u2.getValue(), u3.getValue(), u4.getValue(), u5.getValue(), u6.getValue(), u7.getValue(), u8.getValue(),
      d1.getValue(), d2.getValue(), d3.getValue(), d4.getValue(), d5.getValue(), d6.getValue(), d7.getValue(), d8.getValue())  
  }
  def ac2_ReadUKT38sh4_trp(ch:Int, speed:Int):UKT38 = {
    val t1 = new FloatByReference(0f); val u1 = new FloatByReference(0f); val d1 = new FloatByReference(0f)
    val t2 = new FloatByReference(0f); val u2 = new FloatByReference(0f); val d2 = new FloatByReference(0f)
    val t3 = new FloatByReference(0f); val u3 = new FloatByReference(0f); val d3 = new FloatByReference(0f)
    val t4 = new FloatByReference(0f); val u4 = new FloatByReference(0f); val d4 = new FloatByReference(0f)
    val t5 = new FloatByReference(0f); val u5 = new FloatByReference(0f); val d5 = new FloatByReference(0f)
    val t6 = new FloatByReference(0f); val u6 = new FloatByReference(0f); val d6 = new FloatByReference(0f)
    val t7 = new FloatByReference(0f); val u7 = new FloatByReference(0f); val d7 = new FloatByReference(0f)
    val t8 = new FloatByReference(0f); val u8 = new FloatByReference(0f); val d8 = new FloatByReference(0f)
    ifError(dll.AC2_ReadUKT38sh4_trp(ch, speed, t1, t2, t3, t4, t5, t6, t7, t8, u1, u2, u3, u4, u5, u6, u7, u8, d1, d2, d3, d4, d5, d6, d7, d8))
    new UKT38(t1.getValue(), t2.getValue(), t3.getValue(), t4.getValue(), t5.getValue(), t6.getValue(), t7.getValue(), t8.getValue(), 
      u1.getValue(), u2.getValue(), u3.getValue(), u4.getValue(), u5.getValue(), u6.getValue(), u7.getValue(), u8.getValue(),
      d1.getValue(), d2.getValue(), d3.getValue(), d4.getValue(), d5.getValue(), d6.getValue(), d7.getValue(), d8.getValue())    
  }
  def ac2_ReadTRM32(ch:Int):TRM32 = {
    val UseDirectWater = new IntByReference(0)
    val Taero = new FloatByReference(0f); val Tobr = new FloatByReference(0f)
    val Totop = new FloatByReference(0f); val Tgvs = new FloatByReference(0f)
    val TustObr = new FloatByReference(0f); val TustOtop = new FloatByReference(0f)
    val TustGvs = new FloatByReference(0f) 
    ifError(dll.AC2_ReadTRM32(ch, Taero, Tobr, Totop, Tgvs, TustObr, TustOtop, TustGvs, UseDirectWater))
    new TRM32(Taero.getValue(), Tobr.getValue(), Totop.getValue(), Tgvs.getValue(), TustObr.getValue(), TustOtop.getValue(), TustGvs.getValue(), UseDirectWater.getValue())      
  }
  def ac2_ReadTRM33(ch:Int):TRM33 = {
    val t1 = new FloatByReference(0f); val Rshift_TobrMax = new FloatByReference(0f)
    val t2 = new FloatByReference(0f); val TavarMin = new FloatByReference(0f)
    val t3 = new FloatByReference(0f); val TustAero = new FloatByReference(0f)
    val t4 = new FloatByReference(0f); val TobrWaterLow = new FloatByReference(0f)
    val t5 = new FloatByReference(0f) 
    val t6 = new FloatByReference(0f) 
    val t7 = new FloatByReference(0f) 
    val t8 = new FloatByReference(0f) 
    ifError(dll.AC2_ReadTRM33(ch, t1, t2, t3, t4, t5, t6, t7, t8, Rshift_TobrMax, TavarMin, TustAero, TobrWaterLow))
    new TRM33(t1.getValue(), t2.getValue(), t3.getValue(), t4.getValue(), t5.getValue(), t6.getValue(), t7.getValue(), t8.getValue(), 
      Rshift_TobrMax.getValue(), TavarMin.getValue(), TustAero.getValue(), TobrWaterLow.getValue())      
  }
  //Native interface
  private trait owen_io extends Library {
    def SetApiMode(mode:Int):Int
    def OpenPort(n:Int, speed:Int, parity:Int, bits:Int, stop:Int, converter:Int):Int
    def ClosePort():Int
    def SelectPort(n:Int):Int
    def SetupPort(n:Int, speed:Int, parity:Int, bits:Int, stop:Int, converter:Int):Int
    def LastErrToStr(res:Array[Byte]):Int
    def GetExtendedLastErr():Int
    def SetDbgIndication(i:Int):Int
    def GetMaxRetriesGlobal():Int
    def SetMaxRetriesGlobal(NumberOfRetries:Int):Int
    def ReadSInt(adr:Int, adr_type:Int, command:Array[Byte], value:IntByReference, index:Int):Int
    def ReadUInt(adr:Int, adr_type:Int, command:Array[Byte], value:IntByReference, index:Int):Int    
    def WriteByte(adr:Int, adr_type:Int, command:Array[Byte], value:Int, index:Int):Int
    def WriteWord(adr:Int, adr_type:Int, command:Array[Byte], value:Int,index:Int):Int
    def ReadFloat24(adr:Int, adr_type:Int, command:Array[Byte], value:FloatByReference, index:Int):Int
    def ReadStoredDotS(adr:Int, adr_type:Int, command:Array[Byte], value:FloatByReference, index:Int):Int 
    def ReadIEEE32(adr:Int, adr_type:Int, command:Array[Byte], value:FloatByReference, time:IntByReference, index:Int):Int 
    def WriteFloat24(adr:Int, adr_type:Int, command:Array[Byte], value:Float, index:Int):Int
    def WriteStoredDotS(adr:Int, adr_type:Int, command:Array[Byte], value:Float, index:Int):Int
    def WriteIEEE32(adr:Int, adr_type:Int, command:Array[Byte], value:Float, index:Int):Int
    def OwenIO(adr:Int, adr_type:Int, is_read:Int, command:Array[Byte], params:Array[Byte], param_sz:IntByReference):Int
    def ReadDTMR(adr:Int, adr_type:Int, hrs:IntByReference, mins:IntByReference, sec:IntByReference, msec:IntByReference):Int
    def ReadSI8BCD(adr:Int, adr_type:Int, command:Array[Byte], value:IntByReference):Int
    def WriteCSET(adr:Int, adr_type:Int, prc:Int):Int  
    def ReadPKPBCD(adr:Int, adr_type:Int, command:Array[Byte], value:IntByReference):Int    
    def ReadRTC(adr:Int, adr_type:Int, result:Array[Byte]):Int   
    def Listen(timeout:Int, rcv_hash:ShortByReference, rcv_flag:IntByReference, rcv_adr:IntByReference, param_data:Array[Byte], param_sz:IntByReference):Int    
    def GetBufferSInt(buffer:Array[Byte], buffer_size:Int, result:IntByReference):Int
    def GetBufferUInt(buffer:Array[Byte], buffer_size:Int, result:IntByReference):Int
    def GetBufferByte(buffer:Array[Byte], buffer_size:Int, result:ByteByReference):Int
    def GetBufferWord(buffer:Array[Byte], buffer_size:Int, result:ShortByReference):Int
    def GetBufferFloat24(buffer:Array[Byte], buffer_size:Int, result:FloatByReference):Int
    def GetBufferStoredDotS(buffer:Array[Byte], buffer_size:Int, result:FloatByReference):Int
    def GetBufferStoredDotU(buffer:Array[Byte], buffer_size:Int, result:FloatByReference):Int
    def GetBufferStoredIEEE32(buffer:Array[Byte], buffer_size:Int, result:FloatByReference):Int
    def GetBufferDTMR(buffer:Array[Byte], buffer_size:Int, hrs:IntByReference, mins:IntByReference, sec:IntByReference, msec:IntByReference):Int
    def GetBufferSI8BCD(buffer:Array[Byte], buffer_size:Int, result:IntByReference):Int
    def GetBufferPkpBCD(buffer:Array[Byte], buffer_size:Int, result:FloatByReference):Int
    def GetBufferRTC(buffer:Array[Byte], buffer_size:Int, result:Array[Byte]):Int
    def AC2_Open(n:Int):Int
    def AC2_Close():Int
    def AC2_ReadMpr51(ch:Int, speed:Int, t_prod:FloatByReference, t_suhogo:FloatByReference, t_vlag:FloatByReference, otn_vlag:FloatByReference):Int
    def AC2_ReadTRM__PiC(ch:Int, Temperature:FloatByReference, Rele1:IntByReference, Rele2:IntByReference, Ust1:FloatByReference, Ust2:FloatByReference, Delta1:FloatByReference, Delta2:FloatByReference):Int
    def AC2_WriteTRM__PiC(ch:Int, Temperature:FloatByReference, Rele1:IntByReference, Rele2:IntByReference, Ust1:FloatByReference, Ust2:FloatByReference, Delta1:FloatByReference, Delta2:FloatByReference):Int
    def AC2_ReadTRM_UKT_38_T_and_U(ch:Int,
      t1:FloatByReference, t2:FloatByReference, t3:FloatByReference, t4:FloatByReference, t5:FloatByReference, t6:FloatByReference, t7:FloatByReference, t8:FloatByReference,
      u1:FloatByReference, u2:FloatByReference, u3:FloatByReference, u4:FloatByReference, u5:FloatByReference, u6:FloatByReference,  u7:FloatByReference, u8:FloatByReference):Int
    def AC2_ReadUKT38sh4_IU(ch:Int, speed:Int,
      t1:FloatByReference, t2:FloatByReference, t3:FloatByReference, t4:FloatByReference, t5:FloatByReference, t6:FloatByReference, t7:FloatByReference, t8:FloatByReference,
      u1:FloatByReference, u2:FloatByReference, u3:FloatByReference, u4:FloatByReference, u5:FloatByReference, u6:FloatByReference,  u7:FloatByReference, u8:FloatByReference,
      d1:FloatByReference, d2:FloatByReference, d3:FloatByReference, d4:FloatByReference, d5:FloatByReference, d6:FloatByReference, d7:FloatByReference, d8:FloatByReference):Int
    def AC2_ReadUKT38sh4_trp(ch:Int, speed:Int,
      t1:FloatByReference, t2:FloatByReference, t3:FloatByReference, t4:FloatByReference, t5:FloatByReference, t6:FloatByReference, t7:FloatByReference, t8:FloatByReference,
      u1:FloatByReference, u2:FloatByReference, u3:FloatByReference, u4:FloatByReference, u5:FloatByReference, u6:FloatByReference,  u7:FloatByReference, u8:FloatByReference,
      d1:FloatByReference, d2:FloatByReference, d3:FloatByReference, d4:FloatByReference, d5:FloatByReference, d6:FloatByReference, d7:FloatByReference, d8:FloatByReference):Int
    def AC2_ReadTRM32(ch:Int, Taero:FloatByReference, Tobr:FloatByReference, Totop:FloatByReference, Tgvs:FloatByReference, TustObr:FloatByReference, 
      TustOtop:FloatByReference, TustGvs:FloatByReference, UseDirectWater:IntByReference):Int
    def AC2_ReadTRM33(ch:Int, t1:FloatByReference, t2:FloatByReference, t3:FloatByReference, t4:FloatByReference, t5:FloatByReference, 
      t6:FloatByReference, t7:FloatByReference, t8:FloatByReference, Rshift_TobrMax:FloatByReference, TavarMin:FloatByReference, TustAero:FloatByReference, TobrWaterLow:FloatByReference):Int
  }
  //Functions
  private def ifError(e:Int) = {
    val em = e match{
      case -1 => "ERR_INVALID_ARG"
      case -2 => "ERR_NO_RESOURCE"
      case -3 => "ERR_NO_MEMORY"       
      case -4 => "R_RESOURCE_BUSY"
      case -5 => "ERR_INVALID_RESOURCE"
      case -6 => "R_UNSUPPORTED"  
      case -100 => "ERR_IO"
      case -101 => "R_FORMAT"
      case -102 => "R_TIMEOUT"      
      case -103 => "R_INVALID_CRC"
      case -104 => "ERR_NERR"
      case -105 => "R_DEVERR"     
      case -106 => "RR_INVALID_ANSWER"
      case _ => null}
    if(e < 0){
      val a = Array.fill[Byte](400)(0)
      if(dll.LastErrToStr(a:Array[Byte]) < 0){
        throw new OwenIOException(("Error:" + {if(em != null){em}else{e.toString}}), e)}
      else{
        throw new OwenIOException(("Error: " + {if(em != null){em}else{e.toString}} + ", massage: " + Native.toString(a, "Cp1251")), e)}}
  } 
  private def buildCommand(s:String) = {   
    val ca = s.toCharArray()
    var i = 0; while(i < ca.length && i < 31){bCommand(i) = ca(i).toByte; i += 1}
    bCommand(i) = 0
  }
  private def buildText(s:String) = {   
    val ca = s.toCharArray()
    var i = 0; while(i < ca.length && i < 511){text(i) = ca(i).toByte; i += 1}
    text(i) = 0
  }
}

class OwenIOException(val s:String, val n:Int) extends Exception(s)
class Address (val adr:Int, val adr_type:Int)
class Time(val hrs:Int, val mins:Int, val sec:Int, val msec:Int)
class Buffer(val buffer:Array[Byte], val buffer_size:Int)
class Mpr51(val t_prod:Float, val t_suhogo:Float, val t_vlag:Float, val otn_vlag:Float)
class TRM(val Temperature:Float, val Rele1:Int, val Rele2:Int, val Ust1:Float, val Ust2:Float, val Delta1:Float, val Delta2:Float)
class TRMUKT38(val t1:Float, val t2:Float, val t3:Float, val t4:Float, val t5:Float, val t6:Float, val t7:Float, val t8:Float,
  val u1:Float, val u2:Float, val u3:Float, val u4:Float, val u5:Float, val u6:Float, val u7:Float, val u8:Float)
class UKT38(val t1:Float, val t2:Float, val t3:Float, val t4:Float, val t5:Float, val t6:Float, val t7:Float, val t8:Float,
  val u1:Float, val u2:Float, val u3:Float, val u4:Float, val u5:Float, val u6:Float, val u7:Float, val u8:Float,
  val d1:Float, val d2:Float, val d3:Float, val d4:Float, val d5:Float, val d6:Float, val d7:Float, val d8:Float)
class TRM32(val Taero:Float, val Tobr:Float, val Totop:Float, val Tgvs:Float, val TustObr:Float, val TustOtop:Float, val TustGvs:Float, val UseDirectWater:Int) 
class TRM33(val t1:Float, val t2:Float, val t3:Float, val t4:Float, val t5:Float, val t6:Float, val t7:Float, val t8:Float, 
  val Rshift_TobrMax:Float, val TavarMin:Float, val TustAero:Float, val TobrWaterLow:Float) 
class Command(val command:String, val index:Int)
class Port(val num:Int, val speed:Int, val parity:Int, val bits:Int, val stop:Int, val converter:Int){
  override def equals(a:Any):Boolean = {
    if(a.isInstanceOf[Port]){
      val p = a.asInstanceOf[Port]
      num == p.num &&
      speed == p.speed &&
      parity == p.parity &&
      bits == p.bits &&
      stop == p.stop &&
      converter == p.converter}
    else{
      false}
  }
}
object IOConst {
  val COM_1:Int = 0
  val COM_2:Int = 1
  val COM_3:Int = 2
  val COM_4:Int = 3
  val COM_5:Int = 4
  val COM_6:Int = 5
  val COM_7:Int = 6
  val COM_8:Int = 7
  val spd_300:Int = -3
  val spd_600:Int = -2
  val spd_1200:Int = -1
  val spd_2400:Int = 0
  val spd_4800:Int = 1
  val spd_9600:Int = 2
  val spd_14400:Int = 3
  val spd_19200:Int = 4
  val spd_28800:Int = 5
  val spd_38800:Int = 6
  val spd_57600:Int = 7
  val spd_115200:Int = 8
  val prty_NONE:Int = 0
  val prty_EVEN:Int = 1
  val prty_ODD:Int = 2
  val databits_7:Int = 0
  val databits_8:Int = 1
  val stopbit_1:Int = 0
  val stopbit_1_5:Int = 1
  val stopbit_2:Int = 2
  val RS485CONV_MANUAL:Int = 0
  val RS485CONV_AUTO:Int = 1
  val RS485CONV_MANUAL_DTR:Int = 2
  val ADRTYPE_8BIT:Int = 0
  val ADRTYPE_11BIT:Int = 1
  val OWENIO_API_OLD:Int = 0
  val OWENIO_API_NEW:Int = 1
  val SHOW_PACKETS:Int = 1
  val SHOW_SEND_DATA:Int = 2
  val SHOW_CONTAINS:Int = 4
  val SHOW_RVCED_DATA:Int = 8
  val SHOW_RCV_ERRORS:Int = 16
  val SHOW_RCV_ALL:Int = 32
  val ERR_OK:Int = 0
  val ERR_INVALID_ARG:Int = -1
  val ERR_NO_RESOURCE:Int = -2
  val ERR_NO_MEMORY:Int = -3
  val R_RESOURCE_BUSY:Int = -4
  val ERR_INVALID_RESOURCE:Int = -5
  val R_UNSUPPORTED:Int = -6
  val ERR_IO:Int = -100
  val R_FORMAT: Int = -101
  val R_TIMEOUT:Int = -102
  val R_INVALID_CRC:Int = -103
  val ERR_NERR:Int = -104
  val R_DEVERR:Int = -105
  val RR_INVALID_ANSWER:Int = -106  
}


































 