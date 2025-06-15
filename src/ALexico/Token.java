package ALexico;

public class Token {
	private String codigo;
	private String atributo;
	
	public Token(String codigo, String atributo) {
		this.codigo= codigo;
		this.atributo = atributo;
	}
	
	public String getCodigo() {
		return codigo;
	}
	
	public void setCodigo(String codigoNuevo) {
		this.codigo = codigoNuevo;
	}
	
	public String getAtributo() {
		return atributo;
	}
	
	public void setAtributo(String atributoNuevo) {
		this.atributo = atributoNuevo;
	}
	
	@Override
	public String toString() {
		return "<" + codigo + "," + atributo + ">";
	}
}
