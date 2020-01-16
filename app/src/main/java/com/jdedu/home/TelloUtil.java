package com.jdedu.home;

public class TelloUtil {
    public static void DLog(String strMsg) {
        String strThreadInfo = String.format("[Thread-ID:%05d] ", Thread.currentThread().getId());
        System.out.println(strThreadInfo + strMsg);
    }

    public static void DMsg(String strFormat, Object... objArgs) {
        DLog(String.format(strFormat, objArgs));
    }

    private static final int HELLO_COLUMN_SIZE = 16;
    public static void DBuffer(String strName, byte baBuffer[]) {
        if (baBuffer == null) {
            return;
        }

        DBuffer(strName, baBuffer, baBuffer.length);
    }

    public static void DBuffer(String strName, byte baBuffer[], int iSize) {
        DBuffer(strName, baBuffer, 0, iSize);
    }

    public static void DBuffer(String strName, byte[] baBuffer, int iOffset, int iSize)
    {
        try {
            if (baBuffer == null) {
                DLog("BUFFER[] is NULL!!");
                return;
            }

            if (baBuffer.length > 0) {
                StringBuffer sbHex = new StringBuffer();
                int iPadlength = iSize + ((iSize % HELLO_COLUMN_SIZE) == 0 ? 0 : HELLO_COLUMN_SIZE - (iSize % HELLO_COLUMN_SIZE));
                int iAryMax = iSize;
                int iColCnt = 0;

                DLog("[" + strName + "] Length = " + iSize);
                DLog("=============================================================================");
                DLog(" Offset   00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F");
                DLog("-----------------------------------------------------------------------------");
                for (int i = iOffset; i < iOffset + iPadlength; i++) {
                    if (i < (iAryMax + iOffset)) {
                        sbHex.append(String.format("%02X", baBuffer[i]));
                    }
                    else {
                        sbHex.append("  ");
                    }

                    sbHex.append(" ");
                    if ((++iColCnt % HELLO_COLUMN_SIZE) == 0) {
                        sbHex.append(": ");
                        for (int iHex = 0; iHex < HELLO_COLUMN_SIZE; iHex++) {
                            if (i - (HELLO_COLUMN_SIZE - 1) + iHex < iAryMax) {
                                if ((baBuffer[i - (HELLO_COLUMN_SIZE - 1) + iHex] < 0x20) || (baBuffer[i - (HELLO_COLUMN_SIZE - 1) + iHex] > 0x7F)) {
                                    sbHex.append("^");
                                }
                                else {
                                    sbHex.append((char)baBuffer[i - (HELLO_COLUMN_SIZE - 1) + iHex]);
                                }
                            }
                        }

                        DLog(String.format("%08X", i - (HELLO_COLUMN_SIZE - 1)) + "  " + sbHex.toString());
                        sbHex.delete(0, sbHex.length());
                    }
                }
                DLog("=============================================================================");
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }
}
