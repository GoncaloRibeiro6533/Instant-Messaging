package pt.isel

interface TransactionManager {
    /**
     * This method creates an instance of pt.isel.Transaction, potentially
     * initializing a JDBC Connection,a JDBI Handle, or another resource,
     * which is then passed as an argument to the pt.isel.Transaction constructor.
     */
    fun <R> run(block: Transaction.() -> R): R
}
