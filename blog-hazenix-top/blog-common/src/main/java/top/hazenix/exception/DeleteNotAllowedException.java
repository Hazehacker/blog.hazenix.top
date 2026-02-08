package top.hazenix.exception;

public class DeleteNotAllowedException extends BussinessException {
    public DeleteNotAllowedException(String a04001, String deleteNotAllowedThisCategoryHasArticles) {
        super(a04001, deleteNotAllowedThisCategoryHasArticles);
    }
}
