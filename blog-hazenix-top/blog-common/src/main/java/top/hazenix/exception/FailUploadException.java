package top.hazenix.exception;


public class FailUploadException extends BussinessException {

    public FailUploadException(String b00004, String uploadFailed) {
        super(b00004, uploadFailed);
    }
}
