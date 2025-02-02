package exceptions

case class InvalidSessionException(exMsg: String) extends Exception(exMsg)
