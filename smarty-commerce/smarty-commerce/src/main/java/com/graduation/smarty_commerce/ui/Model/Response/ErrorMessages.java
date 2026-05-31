package com.graduation.smarty_commerce.ui.Model.Response;

public enum ErrorMessages {

    MISSING_REQUIRED_FIELD("Missing required field. Please check documentation for required fields"),
    RECORD_ALREADY_EXISTS("Record already exists"),
    INTERNAL_SERVER_ERROR("Internal server error"),
    NO_RECORD_FOUND("Record with provided id is not found"),
    AUTHENTICATION_FAILED("Authentication failed"),
    COULD_NOT_UPDATE_RECORD("Could not update record"),
    COULD_NOT_DELETE_RECORD("Could not delete record"),
    EMAIL_ADDRESS_NOT_VERIFIED("Email address could not be verified"),
    INVALID_PAGE_NUMBER("Page number cannot be less than 0."),
    USER_NOT_FOUND("User not found!"),
    CART_IS_EMPTY("Cart is empty!"),
    NOT_ENOUGH_STOCK("Not enough stock for product"),
    ORDER_NOT_FOUND("Order not found!"),
    UNAUTHORIZED_ORDER_ACTION("Order does not belong to this user!"),
    ORDER_CANCELLATION_NOT_ALLOWED("Only PENDING orders can be cancelled!"),
    ORDER_DELETION_NOT_ALLOWED("Only PENDING and CANCELLED orders can be deleted!"),
    PRODUCT_NOT_FOUND("Product not found!"),
    CART_ITEM_NOT_FOUND("Cart item not found!");


    private String errorMessage;

    ErrorMessages(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
