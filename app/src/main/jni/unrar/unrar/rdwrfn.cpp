#include "rar.hpp"

ComprDataIO::ComprDataIO()
{
#ifndef RAR_NOCRYPT
  Crypt=new CryptData;
  Decrypt=new CryptData;
#endif

  Init();
}


void ComprDataIO::Init()
{
  UnpackFromMemory=false;
  UnpackToMemory=false;
  UnpPackedSize=0;
  UnpPackedLeft=0;
  ShowProgress=true;
  TestMode=false;
  SkipUnpCRC=false;
  NoFileHeader=false;
  PackVolume=false;
  UnpVolume=false;
  NextVolumeMissing=false;
  SrcFile=NULL;
  DestFile=NULL;
  UnpWrAddr=NULL;
  UnpWrSize=0;
  Command=NULL;
  Encryption=false;
  Decryption=false;
  CurPackRead=CurPackWrite=CurUnpRead=CurUnpWrite=0;
  LastPercent=-1;
  SubHead=NULL;
  SubHeadPos=NULL;
  CurrentCommand=0;
  ProcessedArcSize=0;
  LastArcSize=0;
  TotalArcSize=0;
}


ComprDataIO::~ComprDataIO()
{
#ifndef RAR_NOCRYPT
  delete Crypt;
  delete Decrypt;
#endif
}




int ComprDataIO::UnpRead(byte *Addr,size_t Count)
{
#ifndef RAR_NOCRYPT
  // In case of encryption we need to align read size to encryption 
  // block size. We can do it by simple masking, because unpack read code
  // always reads more than CRYPT_BLOCK_SIZE, so we do not risk to make it 0.
  if (Decryption)
    Count &= ~CRYPT_BLOCK_MASK;
#endif
  
  int ReadSize=0,TotalRead=0;
  byte *ReadAddr;
  ReadAddr=Addr;
  while (Count > 0)
  {
    if (UnpackFromMemory)
    {
      size_t SizeToRead = ((int64)Count > UnpPackedSize) ? (size_t)UnpPackedSize : Count;
      memcpy( Addr, UnpackFromMemoryAddr, SizeToRead );
      ReadSize = (int)SizeToRead;
      UnpackFromMemorySize -= SizeToRead;
      UnpackFromMemoryAddr += SizeToRead;
    }
    else abort();
    CurUnpRead+=ReadSize;
    TotalRead+=ReadSize;
#ifndef NOVOLUME
    // These variable are not used in NOVOLUME mode, so it is better
    // to exclude commands below to avoid compiler warnings.
    ReadAddr+=ReadSize;
    Count-=ReadSize;
#endif
    UnpPackedLeft-=ReadSize;
    break;
  }
  if (ReadSize!=-1)
  {
    ReadSize=TotalRead;
#ifndef RAR_NOCRYPT
    if (Decryption)
      Decrypt->DecryptBlock(Addr,ReadSize);
#endif
  }
  return ReadSize;
}


void ComprDataIO::UnpWrite(byte *Addr,size_t Count)
{

#ifdef RARDLL
  CommandData *Cmd=((Archive *)SrcFile)->GetCommandData();
  if (Cmd->DllOpMode!=RAR_SKIP)
  {
    if (Cmd->Callback!=NULL &&
        Cmd->Callback(UCM_PROCESSDATA,Cmd->UserData,(LPARAM)Addr,Count)==-1)
      ErrHandler.Exit(RARX_USERBREAK);
    if (Cmd->ProcessDataProc!=NULL)
    {
      int RetCode=Cmd->ProcessDataProc(Addr,(int)Count);
      if (RetCode==0)
        ErrHandler.Exit(RARX_USERBREAK);
    }
  }
#endif // RARDLL

  UnpWrAddr=Addr;
  UnpWrSize=Count;
  if (UnpackToMemory)
  {
    if (Count <= UnpackToMemorySize)
    {
      memcpy(UnpackToMemoryAddr,Addr,Count);
      UnpackToMemoryAddr+=Count;
      UnpackToMemorySize-=Count;
    }
  }
  else abort();
  CurUnpWrite+=Count;
  if (!SkipUnpCRC)
    UnpHash.Update(Addr,Count);
  ShowUnpWrite();
}








void ComprDataIO::ShowUnpWrite()
{
}










void ComprDataIO::SetFiles(File *SrcFile,File *DestFile)
{
  if (SrcFile!=NULL)
    ComprDataIO::SrcFile=SrcFile;
  if (DestFile!=NULL)
    ComprDataIO::DestFile=DestFile;
  LastPercent=-1;
}


void ComprDataIO::GetUnpackedData(byte **Data,size_t *Size)
{
  *Data=UnpWrAddr;
  *Size=UnpWrSize;
}


void ComprDataIO::SetEncryption(bool Encrypt,CRYPT_METHOD Method,
     SecPassword *Password,const byte *Salt,const byte *InitV,
     uint Lg2Cnt,byte *HashKey,byte *PswCheck)
{
#ifndef RAR_NOCRYPT
  if (Encrypt)
    Encryption=Crypt->SetCryptKeys(true,Method,Password,Salt,InitV,Lg2Cnt,HashKey,PswCheck);
  else
    Decryption=Decrypt->SetCryptKeys(false,Method,Password,Salt,InitV,Lg2Cnt,HashKey,PswCheck);
#endif
}


#if !defined(SFX_MODULE) && !defined(RAR_NOCRYPT)
void ComprDataIO::SetAV15Encryption()
{
  Decryption=true;
  Decrypt->SetAV15Encryption();
}
#endif


#if !defined(SFX_MODULE) && !defined(RAR_NOCRYPT)
void ComprDataIO::SetCmt13Encryption()
{
  Decryption=true;
  Decrypt->SetCmt13Encryption();
}
#endif




void ComprDataIO::SetUnpackToMemory(byte *Addr,uint Size)
{
  UnpackToMemory=true;
  UnpackToMemoryAddr=Addr;
  UnpackToMemorySize=Size;
}


void ComprDataIO::SetUnpackFromMemory(byte *Addr,uint Size)
{
  UnpackFromMemory=true;
  UnpackFromMemoryAddr=Addr;
  UnpackFromMemorySize=Size;
}
