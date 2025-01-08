package com.ingescape;

public enum IopType {
	IGS_INTEGER_T, 
	IGS_DOUBLE_T,  
	IGS_STRING_T,
	IGS_BOOL_T,
	IGS_IMPULSION_T,
	IGS_DATA_T;
	
	public static IopType fromInt(int i) {
        switch(i) {
        case 1:
            return IGS_INTEGER_T;
        case 2:
            return IGS_DOUBLE_T;
        case 3:
            return IGS_STRING_T;
        case 4:
            return IGS_BOOL_T;
        case 5:
            return IGS_IMPULSION_T;
        case 6:
            return IGS_DATA_T;
        }
        return null;
    }
	public static int toInt(IopType type) {
        switch(type) {
        case IGS_INTEGER_T:
            return 1;
        case IGS_DOUBLE_T:
            return 2;
        case IGS_STRING_T:
            return 3;
        case IGS_BOOL_T:
            return 4;
        case IGS_IMPULSION_T:
            return 5;
        case IGS_DATA_T:
            return 6;
        }
        return 0;
    }
	public static String toNormalizedName(IopType type) {
		switch (type) {
		case IGS_INTEGER_T:
			return("INTEGER");
		case IGS_DOUBLE_T:
			return("DOUBLE");
		case IGS_BOOL_T:
			return("BOOL");
		case IGS_IMPULSION_T:
			return("IMPULSION");
		case IGS_STRING_T:
			return("STRING");
		case IGS_DATA_T:
			return("DATA");

		default:
			return null;
		}
	}
}
