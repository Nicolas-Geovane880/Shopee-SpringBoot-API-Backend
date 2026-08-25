package nicolas.shopee_label_calculator.utils;

public enum OrderStatus {

    CANCELLED, DELIVERED, SENT, TO_SEND, CONCLUDED, NOT_PAID, NOT_LISTED, REQUEST_RETURN, ACCEPTED_REFUND, UNACCEPTED_REFUND, REFUND_IN_PROGRESS;

    public static OrderStatus getOrderStatus (String orderStatusStr) {
        if (orderStatusStr.contains ("O comprador pode pedir uma devolução até")) return DELIVERED;

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

    public static String getOrderStatusStr (OrderStatus orderStatus) {

        switch (orderStatus) {
            case CANCELLED -> {return "Cancelado";}
            case TO_SEND -> {return "Para enviar";}
            case SENT -> {return "Enviado";}
            case CONCLUDED -> {return "Concluído";}
            case NOT_PAID -> {return "Não pago";}
            case DELIVERED -> {return "Entregue";}
            case NOT_LISTED -> {return "Não listado";}
            case REQUEST_RETURN -> {return "Devolução solicitada";}
            case ACCEPTED_REFUND -> {return "Devolução aprovada";}
            case UNACCEPTED_REFUND -> {return "Devolução rejeitada";}
            case REFUND_IN_PROGRESS -> {return "Devolução em andamento";}
            case null, default -> {
                return "Desconhecido";
            }
        }
    }
}
