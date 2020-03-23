package com.Swed;

import com.sun.imageio.plugins.jpeg.*;
import com.sun.imageio.plugins.png.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.util.BitSet;

public class Image {


    public static void main(String[] args) throws IOException {
        int step = 500;
        BufferedImage CVZ = readFromFile("Толя CVZ.jpg");
        int height = CVZ.getHeight();
        int width  = CVZ.getWidth();
        int[] pixels = copyFromBufferedImage(CVZ,height,width);
        BufferedImage picture = readFromFile("Messi.jpg");
        int heightPct = picture.getHeight();
        int widthPct  = picture.getWidth();
        int[] pixelsPct = copyFromBufferedImage(picture,heightPct,widthPct);
        int[] container = copyFromBufferedImage(picture,heightPct,widthPct);
        if(pixelsPct.length > pixels.length)System.out.println("ЦВЗ влезает в картнику");
       /* int[] StegoConteiner = enCrypt(pixelsPct,heightPct,widthPct,pixels,step);
        BufferedImage Stego = copyToBufferedImage(widthPct, heightPct, StegoConteiner);
        File outputfile = new File("Stego.jpg");
        ImageIO.write(Stego, "jpg", outputfile);
        double mse = MSE(container,StegoConteiner);
        double nmse = NMSE(container,StegoConteiner);
        PSNR(mse,container,StegoConteiner);
        int[] extractCvz = extractionCVZ(StegoConteiner,heightPct,widthPct,step,height);
        BufferedImage extract = copyToBufferedImage(width,height,extractCvz);
        File outputfile1 = new File("extract.jpg");
        ImageIO.write(extract, "jpg", outputfile1);
        SNR(heightPct,widthPct,container,StegoConteiner);*/
        int[] StegoContainer = enCryptLSB(pixelsPct,pixels);
        BufferedImage St = copyToBufferedImage(widthPct, heightPct, StegoContainer);
        File outputfile2 = new File("StegoContLSB.jpg");
        ImageIO.write(St, "jpg", outputfile2);
        int[] extractC = extractLSB(StegoContainer,height,width);
        BufferedImage extractLSB = copyToBufferedImage(width, height, extractC);
        File outputfile3 = new File("LSB.jpg");
        ImageIO.write(extractLSB, "jpg", outputfile3);
        double mse2 = MSE(container,StegoContainer);
        double nmse2 = NMSE(container,StegoContainer);
        PSNR(mse2,container,StegoContainer);
        SNR(heightPct,widthPct,container,StegoContainer);
    }


    private int     height;             // высота изображения
    private int     width;              // ширина изображения
    private int[]   pixels;             // собственно массив цветов точек составляющих изображение

    public int getPixel(int x, int y)   { return pixels[y*width+x]; }   // получить пиксель в формате RGB
    public static int getRed(int color)        { return color >> 16; }         // получить красную составляющую цвета
    public static int getGreen(int color)      { return (color >> 8) & 0xFF; } // получить зеленую составляющую цвета
    public static int getBlue(int color)       { return color  & 0xFF;}        // получить синюю   составляющую цвета

    //Оценка MSE
    private static double MSE(int[] pict, int[] stego){
        double summ = 0;
        for (int i = 0; i < pict.length; i++){
            summ += Math.abs(Math.pow((pict[i] - stego[i]),2));
        }
        double mse = summ / pict.length;
        System.out.println("MSE = " + mse);
        return mse;
    }

    //Оценка NMSE
    private static double NMSE(int[] pict, int[] stego){
        double nmse = 0;
        double summ = 0;
        for (int i = 0; i < pict.length; i++){
            summ += Math.pow(pict[i],2);
        }
        for(int j = 0; j < pict[j]; j++){
            nmse += Math.pow((pict[j] - stego[j]),2) / summ;
        }
        System.out.println("NMSE = " + nmse);
        return nmse;
    }

    //Оценка PSNR
    private static void PSNR(double mse, int[] pict, int[] stego){
        int maxValue = 0;
        double summ = 0;
        for (int i = 0; i < pict.length; i++){
            if(maxValue < pict[i]) maxValue = pict[i];
            summ = ( summ +  Math.pow((pict[i] - stego[i]), 2));
        }
        double PSNR2 = 10 * Math.log10(Math.pow(maxValue,2) / mse);
        double PSNR = ((pict.length * Math.pow(maxValue, 2)) / summ);
        System.out.println("PSNR = " + PSNR);
        System.out.println("PSNR2 = " + PSNR2);
    }

    //Оценка SNR
    private static void SNR(int height, int width, int[] pict, int[] stego){
        double summ1 = 0;
        double summ = 0;
        for (int i = 0; i < pict.length; i++){
            summ1 = summ1 + Math.pow(pict[i],2);
            summ = ( summ +  Math.pow((pict[i] - stego[i]), 2));
        }
        double SNR = 10 * Math.log10(summ1 / summ);
        System.out.println("SNR = " + SNR );
    }
    //Подсчет шагов для метода ПСИ
   /* private static int[] stepCounting(int height,int width, int key){
        int[] step = new int[height * width];
        for (int i = 1; i < height; i++){
            int summ = 0;
            for(int j = 0; j < i; j++) {
                summ += j;
            }
            step[i] = key * summ;
        }
        return step;
    }
    //Встраивание метод НЗБ PSI
    private static int[] enCryptLSBWithPSI(int[] pict, int[] CVZ, int[] step){
        int counter = 0;
        int bit = 0;
        for(int i = 0; i < pict.length;){
            if(bit == CVZ.length) break;
                BitSet bs = BitSet.valueOf(new long[] { pict[i] });
                if(CVZ[bit] == 0){
                    bs.clear(0);
                }else{
                    bs.set(0);
                }
            pict[i] = bits2Ints(bs);
            bit += 1;
            i += step[counter];
            counter++;
        }
        return pict;
    }

    //Извлечение ЦВЗ метод НЗБ PSI
    private static int[] extractLSBPSI(int[] pict, int height, int width, int[] step){
        int counter = 0;
        int[] extract = new int[height * width];
        int size = height * width;
        int bit = 0;
        for(int i = 0; i < pict.length;bit++){
            if(bit == size)break;
                BitSet bs = BitSet.valueOf(new long[] { pict[i] });
                if(bs.get(0)){
                    extract[bit] = 16777215;
                }else{
                    extract[bit] = 0;
                }
            i += step[counter];
            counter++;
        }
        return extract;
    }*/

   //Встраивание метод НЗБ
    private static int[] enCryptLSB(int[] pict, int[] CVZ){
        int bit = 0;
        for(int i = 0; i < pict.length; i++){
            if(bit == CVZ.length) break;
            int pos = 0;
                BitSet bs = BitSet.valueOf(new long[] { pict[i] });
                if(CVZ[bit] == 0){
                    bs.clear(pos);
                    pict[i] = bits2Ints(bs);
                    bit += 1;
                    }else{
                    bs.set(pos);
                    pict[i] = bits2Ints(bs);
                    bit += 1;
                    }
                if(bit == CVZ.length) break;
        }
        return pict;
    }

    static int bits2Ints(BitSet bs) {
        int[] temp = new int[bs.size() / 32];

        for (int i = 0; i < temp.length; i++)
            for (int j = 0; j < 32; j++)
                if (bs.get(i * 32 + j))
                    temp[i] |= 1 << j;

        return temp[0];
    }

    //Извлечение ЦВЗ метод НЗБ
    private static int[] extractLSB(int[] pict, int height, int width){
        int[] extract = new int[height * width];
        int size = height * width;
        int bit = 0;
        for(int i = 0; i < pict.length; i++){
            if(bit == size)break;
            int pos = 0;
                BitSet bs = BitSet.valueOf(new long[] { pict[i] });
                if(bs.get(pos)){
                    extract[bit] = 16777215;
                }else{
                    extract[bit] = 0;
                }
                bit++;
                if(bit == size)break;

        }
        return extract;
    }

   //Встраивание ЦВЗ метод Куттера
    private static int[] enCrypt(int[] pict, int height, int width, int[] CVZ, int step){
        int i = 0;
        int bit = 0;
            for (; i < height*width; bit++) {
                if(CVZ[bit] == 0){
                    int newBlue = (int) (getBlue(pict[i]) - 0.1 * (0.3 * getRed(pict[i]) +
                            0.59 * getGreen(pict[i]) + 0.11 * getBlue(pict[i])));
                    if (newBlue > 255) newBlue=255;  // Отсекаем при превышении границ байта
                    if (newBlue < 0)   newBlue=0;
                    pict[i] = pict[i] & 0xFFFF00 | (newBlue);
                    if(bit == CVZ.length-1) break;
                }else{
                    int newBlue = (int) (getBlue(pict[i]) + 0.1 * (0.3 * getRed(pict[i]) +
                            0.59 * getGreen(pict[i]) + 0.11 * getBlue(pict[i])));
                    if (newBlue > 255) newBlue=255;  // Отсекаем при превышении границ байта
                    if (newBlue < 0)   newBlue=0;
                    pict[i] = pict[i] & 0xFFFF00 | (newBlue);
                    if(bit == CVZ.length-1) break;
                }
                i += step;
            }
        return pict;
    }

    //Извлечение ЦВЗ метод Куттера
    private static int[] extractionCVZ (int[] stegoCont, int height, int width,int step, int size) {
        int[] extractionCVZ = new int[size*size];
        int bit = 0;
        int sosedi = 0;
        int i = 0;
        //for (int bit = 0; bit < size; bit++) {
        for (; i < height*width;) {
                if(i != 0 && (i!=height-1)){
                    sosedi = (getBlue(stegoCont[i + 1]) + getBlue(stegoCont[i - 1]) +
                            getBlue(stegoCont[(i - 1 + width)]) + getBlue(stegoCont[(i + 1 + width)])) / 4;
                }else if(i == 0){
                    sosedi = (getBlue(stegoCont[i + 1])
                            + getBlue(stegoCont[(i + 1) * width])) / 4;
                }else if(i != height-1) {
                    sosedi = (getBlue(stegoCont[i + 1]) + getBlue(stegoCont[i - 1])
                            + getBlue(stegoCont[(i + 1) * width])) / 4;
                }
                if(getBlue(stegoCont[i]) > sosedi){
                    //System.out.println("white");
                    if(bit > size*size-1 )break;
                    extractionCVZ[bit] = 16777215;
                    bit +=1;
                }else{
                    //System.out.println("black");
                    if(bit > size*size-1 )break;
                    extractionCVZ[bit] = 0;
                    bit +=1;
                }
                i += step;
            }
        // }
        return extractionCVZ;
    }

   private static void vivod(int[] pixels){
       for (int i = 0; i < pixels.length;i++){
           System.out.print(pixels[i] + " ");
       }
         System.out.println("\n");
   }

   //Нахождение среднего значения синего соседних пикселей
   /* private static int avarageBlue(int[] stegoCont, int height, int width){

        int sosedi = (getBlue(stegoCont[i * width + j + 1]) + getBlue(stegoCont[i * width + j - 1]) +
                getBlue(stegoCont[(i - 1) * width + j]) + getBlue(stegoCont[(i - 1) * width + j])) / 4;
    }*/

    // Чтение изображения из файла в BufferedImage
    private static BufferedImage readFromFile(String fileName) throws IOException {
        ImageReader     r  = new JPEGImageReader(new JPEGImageReaderSpi());
        r.setInput(new FileImageInputStream(new File(fileName)));
        BufferedImage  bi = r.read(0, new ImageReadParam());
        ((FileImageInputStream) r.getInput()).close();
        return bi;
    }

    private static BufferedImage readFromFilePng(String fileName) throws IOException {
        ImageReader     r  = new PNGImageReader(new PNGImageReaderSpi());
        r.setInput(new FileImageInputStream(new File(fileName)));
        BufferedImage  bi = r.read(0, new ImageReadParam());
        ((FileImageInputStream) r.getInput()).close();
        return bi;
    }

    // Формирование BufferedImage из массива pixels
    private static BufferedImage copyToBufferedImage(int width, int height, int[] pixels)  {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                bi.setRGB(j, i, pixels[i*width +j]);
        return bi;
    }

    // Формирование массива пикселей из BufferedImage
    public static int[] copyFromBufferedImage(BufferedImage bi,int height, int width)  {
        int[] pict = new int[height*width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                pict[i*width + j] = bi.getRGB(j, i) & 0xFFFFFF; // 0xFFFFFF: записываем только 3 младших байта RGB
        return pict;
    }

    // Запись изображения в jpeg-формате
  /*  public void saveAsJpeg(String fileName) throws IOException {
        ImageWriter writer = new JPEGImageWriter(new JPEGImageWriterSpi());
        saveToImageFile(writer, fileName);
    }

    // Запись изображения в png-формате (другие графические форматы по аналогии)
    public void saveAsPng(String fileName) throws IOException {
        ImageWriter writer = new PNGImageWriter(new PNGImageWriterSpi());
        saveToImageFile(writer, fileName);
    }*/

    // Собственно запись файла (общая для всех форматов часть).
    /*private void saveToImageFile(ImageWriter iw, String fileName) throws IOException {
        iw.setOutput(new FileImageOutputStream(new File(fileName)));
        iw.write(copyToBufferedImage());
        ((FileImageOutputStream) iw.getOutput()).close();
    }*/

    // конвертация изображения в негатив
    public static int[]  convertToNegative(int height, int width, int[] pixels) {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                // Применяем логическое отрицание и отбрасываем старший байт
                pixels[i*width + j] = ~pixels[i*width + j] & 0xFFFFFF;
            return pixels;
    }

    // конвертация изображения в черно-белый вид
    public void convertToBlackAndWhite() {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                // находим среднюю арифметическую интенсивность пикселя по всем цветам
                int intens = (getRed(pixels[i * width + j]) +
                        getGreen(pixels[i * width + j]) +
                        getBlue(pixels[i * width + j])) / 3;
                // ... и записываем ее в каждый цвет за раз , сдвигая байты RGB на свои места
                pixels[i * width + j] = intens + (intens << 8) + (intens << 16);
            }
    }

    // изменяем яркость синего цвета
    public static int[] addColorBlueChannel(int delta,int height, int width, int[]pixels) {
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                int newGreen =  getGreen(pixels[i * width + j]) + delta;
                if (newGreen > 255) newGreen=255;  // Отсекаем при превышении границ байта
                if (newGreen < 0)   newGreen=0;
                // В итоговом пикселе R и B цвета оставляем без изменений: & 0xFF00FF
                // Полученный новый G (зеленый) засунем в "серединку" RGB: | (newGreen << 8)
                pixels[i * width + j] = pixels[i * width + j] & 0xFF00FF | (newGreen << 8);
            }
        return pixels;
    }
}


