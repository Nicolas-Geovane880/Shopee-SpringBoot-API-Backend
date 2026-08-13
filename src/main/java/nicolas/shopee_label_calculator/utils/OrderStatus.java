package nicolas.shopee_label_calculator.utils;

public enum OrderStatus {

    CANCELLED, DELIVERED, SENT, TO_SEND, CONCLUDED, NOT_PAID, NOT_LISTED, REQUEST_RETURN, ACCEPTED_REFUND, UNACCEPTED_REFUND;

    public static OrderStatus getOrderStatus (String orderStatusStr) {
        if (orderStatusStr.contains ("o comprador pode pedir uma devolução até")) return REQUEST_RETURN;

        switch (orderStatusStr.toLowerCase ()) {
            case "cancelado" -> {return  CANCELLED;}
            case "entregue" -> {return DELIVERED;}
            case "enviado" -> {return SENT;}
            case "a enviar" -> {return TO_SEND;}
            case "concluído" -> {return CONCLUDED;}
            case "não pago" -> {return NOT_PAID;}
            default -> {return NOT_LISTED;}
        }
    }
}
