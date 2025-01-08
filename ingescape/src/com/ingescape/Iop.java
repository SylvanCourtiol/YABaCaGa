package com.ingescape;

public enum Iop {
	IGS_INPUT_T, 
	IGS_OUTPUT_T,  
	IGS_PARAMETER_T;
	
	public static Iop fromInt(int i) {
        switch(i) {
        case 1:
            return IGS_INPUT_T;
        case 2:
            return IGS_OUTPUT_T;
        case 3:
            return IGS_PARAMETER_T;
        }
        return null;
    }
	public static int toInt(Iop type) {
        switch(type) {
        case IGS_INPUT_T:
            return 1;
        case IGS_OUTPUT_T:
            return 2;
        case IGS_PARAMETER_T:
            return 3;
        }
        return 0;
    }
}
