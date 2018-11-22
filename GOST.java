import java.io.*;
import java.lang.Math;
import java.util.Scanner;

class GOST{
	public static void main(String args[]){
		long S = 0, G, T, CM5;
		int bit_8 = 0, i;
		long B;
		long siz = 0;

		FileInputStream fin = null;
		FileOutputStream fout = null;
		FileInputStream f_S = null;
		func f = new func();

		if(args.length !=3){
			System.out.println("Использование : исходный_файл конечный_файл ключ");
			return;
		}
		Scanner in = new Scanner(System.in);
		int choice = 0;
		System.out.println("1 - зашифровать, 2 - расшифровать");
		while((choice != 1) && (choice != 2)){
			choice = in.nextInt();
			System.out.println("choice "+choice);
		}

		try{
			fin = new FileInputStream(args[0]);
			fout = new FileOutputStream(args[1]);
			f.inputX(args[2]);

			if(choice == 1){
				S = f.synchro();
	                        for(i = 0; i<8; i++){
        	                        bit_8 = (byte) ((S >> (56-i*8)));
                	                fout.write(bit_8);
                        	}

			}
			else{
				for(i = 0; i<8; i++){
                                        bit_8 = fin.read();
                                        B = bit_8;
                                        B = B << (56-i*8);
                                        S = S | B;
                                }
			}
			G = f.enc(S);

			while (fin.available()>0){
				T = 0;
				for(i = 0; i<8; i++){
					if(fin.available() == 0) break;
					bit_8 = fin.read();
					siz = siz + 1;
					B = bit_8;
					B = B << (56-i*8);
					T = T | B;
				}
				CM5 = T^G;
				for(i = 0; i<siz; i++){
					bit_8 = (byte) (CM5 >> (56-i*8));
					fout.write(bit_8);
				}
				siz = 0;
				if(choice == 1){
					G = f.enc(CM5);
					System.out.println("Encrypting");
				} else {
					G = f.enc(T);
					System.out.println("Decrypting");
				}
			}
		} catch(IOException e){
			System.out.println("Ошибка чтения из файла");
		}
		try{
			fin.close();
			fout.close();
		} catch(IOException e){
			System.out.println("Ошибка закрытия файла");
		}


	}
}


class func{
	int N1, N2, CM1, CM2, R;
	byte temp;
	int i, j;
	long mask = 0xFFFFFFFFL;
	int[] X = { 0, 0, 0, 0, 0, 0, 0, 0};

	void inputX(String arg){
		FileInputStream f_X = null;
		try{
			f_X = new FileInputStream(arg);
			int bit_8, B = 0, x = 0;
			for(i = 0; i<8; i++){
				for(j = 0; j<4; j++){
					bit_8 = f_X.read();
					B = bit_8;
					B = B << (24-j*8);
					x = x | B;
				}
				X[i] = x;
				x = 0;
			}
		} catch (IOException e){
			System.out.println("IOException");
			return;
		}
	}


	byte K[][] = {
		{14,  4, 13,  1,  2, 15, 11,  8,  3, 10,  6, 12,  5,  9,  0,  7},
		{15,  1,  8, 14,  6, 11,  3,  4,  9,  7,  2, 13, 12,  0,  5, 10},
		{10,  0,  9, 14,  6,  3, 15,  5,  1, 13, 12,  7, 11,  4,  2,  8},
		{ 7, 13, 14,  3,  0,  6,  9, 10,  1,  2,  8,  5, 11, 12,  4, 15},
		{ 2, 12,  4,  1,  7, 10, 11,  6,  8,  5,  3, 15, 13,  0, 14,  9},
		{12,  1, 10, 15,  9,  2,  6,  8,  0, 13,  3,  4, 14,  7,  5, 11},
		{ 4, 11,  2, 14, 15,  0,  8, 13,  3, 12,  9,  7,  5, 10,  6,  1},
		{13,  2,  8,  4,  6, 15, 11,  1, 10,  9,  3, 14,  5,  0, 12,  7}
	};

	byte order_crypt[] = 	{ 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 7, 6, 5, 4, 3, 2, 1, 0 };

	long enc( long T){
		N1 = (int) (T >> 32);
		N2 = (int) T;
		N1 = Integer.reverse(N1);
		N2 = Integer.reverse(N2);

		for(i = 0; i<32; i++){
			CM1 = N1+X[order_crypt[i]];
			for(j = 0; j<8; j++){
				temp = (byte) (CM1 >> (28-j*4));
				temp &= 0x0f;
				if(j == 0) R = K[j][temp];
				else R = (R << 4) | K[j][temp];
			}
			R = (R << 11) | ((R>>21) & 0b00000000000111111111111111111111);
                        CM2 = R^N2;
			if(i != 31){	N2 = N1;	N1 = CM2;}
			else {		N1 = N1;	N2 = CM2;}
		}
		N1 = Integer.reverse(N1);
		N2 = Integer.reverse(N2);
		T = N1;
		T = T << 32;
		T = T | (N2 & mask);
		N1 = 0; N2 = 0; CM1 = 0; CM2 = 0; R = 0;
		return T;
	}

	long synchro(){
		long S = 0;
		for(i = 0; i<64; i++){
			S = S | ((int)( Math.random() * 2));
			S = S << 1;
		}
		return S;
	}
}


