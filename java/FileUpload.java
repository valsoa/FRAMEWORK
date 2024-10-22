package util;
public class FileUpload {
   private String nameFile;
   private  byte[] byteFile;
   private String path;
   
    public String getNameFile() {
        return nameFile;
    }
    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }
    public byte[] getByteFile() {
        return byteFile;
    }
    public void setByteFile(byte[] byteFile) {
        this.byteFile = byteFile;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public FileUpload() {
    }
    public FileUpload(String nameFile, String path,byte [] byteFile) {
        this.nameFile = nameFile;
        this.byteFile = byteFile;
        this.path = path;
    }

}
