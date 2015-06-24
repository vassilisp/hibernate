package gq.panop.hibernate.mytypes;

public class TransactionId_Timestamp extends Object{

        private String transactionId=null;
        private Long timestamp=null;
        
        
        public TransactionId_Timestamp(String transactionId, Long timestamp){
            this.transactionId = transactionId;
            this.timestamp = timestamp;
        }
        /**
         * @return the transactionId
         */
        public String getTransactionId() {
            return transactionId;
        }

        /**
         * @param transactionId the transactionId to set
         */
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        /**
         * @return the timestamp
         */
        public Long getTimestamp() {
            return timestamp;
        }

        /**
         * @param timestamp the timestamp to set
         */
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
        
        public String toString(){
            return transactionId.toString() + ", " + timestamp.toString();
        }
}
