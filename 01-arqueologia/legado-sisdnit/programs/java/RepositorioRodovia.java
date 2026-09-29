package br.gov.serpro.sunne.dnit.negocio.persistencia;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.gov.serpro.sunne.dnit.infraestrutura.bd.Transacao;
import br.gov.serpro.sunne.dnit.infraestrutura.excecao.RepositorioException;
import br.gov.serpro.sunne.dnit.negocio.bean.Rodovia;
import br.gov.serpro.sunne.dnit.negocio.bean.Uf;
import oracle.jdbc.OracleTypes;

/**
 * Classe responsável por persistir no BD os dados do Cadastro de Rodovias
 * @author luciano.buzzacaro@serpro.gov.br
 * @date 20/03/2007  
 *
 * @author Endrigo G. Ferreira - endrigo.ferreira@serpro.gov.br
 * @since 07/08/2007
 */

public class RepositorioRodovia implements IRepositorioPadrao<Rodovia> {
	
	public static final Logger LOG = LogManager.getLogger(RepositorioRodovia.class);

	private static final String PROCESSA_VIA = "{call SP_PROCESSA_VIA(?,?,?,?,?,?,?,?,?)}";
	private static final String FILTRA_VIA  = "{call SP_FILTRA_VIA(?,?,?,?,?,?,?)}";
	private static final String CONSULTA_VIA  = "{call SP_CONS_VIA_HID(?,?)}";
	private static final String CONSULTA_VIASG = "{call SP_CONS_VIASG_ST(?,?)}";
	private static final String VERIFICA_SIGLA  = "{call SP_CONS_VIA_SIGLA_EXISTE(?,?)}";
	private static final String FILTRA_VIA_BEM  = "{call SP_CONS_VIA_BEM(?,?)}";
	
	private static RepositorioRodovia singleton;

	public synchronized static RepositorioRodovia getInstancia() {
 		if (singleton == null) {
 			singleton = new RepositorioRodovia();
 		}
 		return singleton;
	}

	/**
	 * @author Endrigo G. Ferreira - endrigo.ferreira@serpro.gov.br
	 * @param bean Uma Rodovia
	 * @return Lista de Rodovia
	 * @throws RepositorioException 
	 * @since 07/08/2007
	 *
	 * @see br.gov.serpro.sunne.dnit.negocio.persistencia.IRepositorioPadrao#filtrar(java.lang.Object)
	 */
	public List<Rodovia> filtrar(Rodovia bean) throws RepositorioException {
		List<Rodovia> lista = new ArrayList<Rodovia>();
		Connection con = null;
		ResultSet rs = null;
		CallableStatement cs = null;
		try {
			con = Transacao.getConnection();
			// Preparando StoreProcedure
			cs = con.prepareCall(RepositorioRodovia.FILTRA_VIA);
			// Define Atributos
			cs.setString(1, bean.getSigla());
			cs.setLong(2, 1);
			cs.setString(3, bean.getDescricao());
			String ufs = "";
			for (Uf uf : bean.getUfs()){
				if (ufs.isEmpty()) {
					ufs+= uf.getId().toString();
				}else {
					ufs+= ";"+uf.getId().toString();
				}
			}
			cs.setString(4, ufs);
			cs.setString(5, "");
			
			if (bean.getSituacao() == null)
				cs.setInt(6, -1);
			else
				cs.setInt(6, bean.getSituacao());
			
			cs.registerOutParameter(7, OracleTypes.CURSOR);

			// Executando a Stored Procedure
			cs.execute();
			rs = (ResultSet) cs.getObject(7);
			List<Uf> listUf = null;
			Rodovia resultado = null;
			HashSet<Uf> setUfs = null;
			Long id = 0L;
			while (rs.next()) {
				if (rs.getLong("CO_VIA") != id) {
					if (resultado != null) {
						Set<Uf> setUfsOrdenado = new TreeSet<Uf>(setUfs);
						listUf.addAll(setUfsOrdenado);
						resultado.setUfs(listUf);
						lista.add(resultado);
					}
					resultado = new Rodovia();
					listUf = new ArrayList<Uf>();
					setUfs = new HashSet<Uf>();
					id = rs.getLong("CO_VIA");
				}
				resultado.setId(id);
				resultado.setDescricao(rs.getString("DE_VIA"));
				resultado.setSigla(rs.getString("SG_VIA"));
				resultado.setSituacao(rs.getInt("ST_ATIVO"));
				if (rs.getLong("CO_UF") > 0) {
					setUfs.add(new Uf(rs.getLong("CO_UF"), rs.getString("NO_UF")));						
				}
			}	
			if (resultado != null) {
				if (resultado.getId() != null) {
					Set<Uf> setUfsOrdenado = new TreeSet<Uf>(setUfs);
					listUf.addAll(setUfsOrdenado);
					resultado.setUfs(listUf);
					lista.add(resultado);						
				}
			}
		}catch(SQLException e){
			throw new RepositorioException(RepositorioException.ERRO_BD, e);
		}finally{
			Transacao.liberarRecursos(cs, rs);
		}
		return lista;
	}

	/**
	 * @author Endrigo G. Ferreira - endrigo.ferreira@serpro.gov.br
	 * @since 07/08/2007
	 *
	 * @see br.gov.serpro.sunne.dnit.negocio.persistencia.IRepositorioPadrao#processar(java.lang.Object)
	 */
	public Rodovia processar(Rodovia bean) throws RepositorioException {
		Connection con = null;
		CallableStatement cs = null;
		try {
			con = Transacao.getConnection();
			cs = con.prepareCall(RepositorioRodovia.PROCESSA_VIA);
			
			/**
			 * 1. P_CO_VIA
			 * 2. P_CO_UNIDADE
			 * 3. P_SG_VIA
			 * 4. P_CO_TIPO_VIA
			 * 5. P_DE_VIA
			 * 6. P_NU_EXTENSAOKM
			 * 7. P_UF_VIA
			 * 8. P_RIO_VIA
			 * 9. P_TP_OPERACAO 
			 */
			
			String ufs = "";
			for (Uf uf : bean.getUfs()){
				if (ufs.isEmpty()) {
					ufs+= uf.getId().toString();
				}else {
					ufs+= ";"+uf.getId().toString();
				}
			}
			
			if (bean.getId() == null){
				cs.setLong(1, 0);
			} else {
				cs.setLong(1, bean.getId());
			}
			cs.setNull(2, OracleTypes.NULL);
			cs.setString(3, bean.getSigla());
			cs.setInt(4, 1); //Tipo de via: rodovia
			cs.setString(5, bean.getDescricao());
			cs.setNull(6, OracleTypes.NULL);
			cs.setString(7, ufs);
			cs.setString(8, "");	
			cs.setInt(9, bean.getSituacao());
			
			cs.registerOutParameter(1, OracleTypes.NUMBER);			
			
			cs.execute();
			
			if (bean.getId()==null||bean.getId().intValue()==0) {
				bean.setId(cs.getLong(1));
			}
		}catch(SQLException e){
			if(e.getErrorCode()==2292){
				throw new RepositorioException(RepositorioException.ERRO_BD_ORA_02292, e, "a UF", "localizações de obras");
			}
			throw new RepositorioException(RepositorioException.ERRO_BD, e);
		}finally{
			Transacao.liberarRecursos(cs);
		}
		return bean;
	}
	
	public List<Rodovia> buscaSgVia(Rodovia bean) throws RepositorioException {
		List<Rodovia> lista = new ArrayList<Rodovia>();
		Connection con = null;
		ResultSet rs = null;
		CallableStatement cs = null;
		try {
			con = Transacao.getConnection();
			// Preparando StoreProcedure
			cs = con.prepareCall(RepositorioRodovia.CONSULTA_VIASG);
			// Define Atributos st_ativo e o retorno
			if (bean.getSituacao() == null)
				cs.setInt(1, -1);
			else
				cs.setInt(1, bean.getSituacao());
			
			cs.registerOutParameter(2, OracleTypes.CURSOR);
			// Executando a Stored Procedure
			cs.execute();
			rs = (ResultSet) cs.getObject(2);
			while (rs.next()) {
				Rodovia resultado = new Rodovia();
				resultado.setId(rs.getLong("CO_VIA"));
				resultado.setSigla(rs.getString("SG_VIA"));
				if (resultado != null) {
					if (resultado.getId() != null) {
						lista.add(resultado);						
					}
				}
			}	

		}catch(SQLException e){
			throw new RepositorioException(RepositorioException.ERRO_BD, e);
		}finally{
			Transacao.liberarRecursos(cs, rs);
		}
		return lista;
	}
	
	public Rodovia buscaID(Long id) throws RepositorioException {
		Rodovia resultado = new Rodovia();
		Connection con = null;
		ResultSet rs = null;
		CallableStatement cs = null;
		try {
			con = Transacao.getConnection();
			// Preparando StoreProcedure
			cs = con.prepareCall(RepositorioRodovia.CONSULTA_VIA);
			// Define Atributos
			cs.setLong(1, id);
			cs.registerOutParameter(2, OracleTypes.CURSOR);
			// Executando a Stored Procedure
			cs.execute();
			rs = (ResultSet) cs.getObject(2);
			List<Uf> listUf = new ArrayList<Uf>();
			HashSet<Uf> setUfs = new HashSet<Uf>();
			while (rs.next()) {
				resultado.setId(rs.getLong("CO_VIA"));
				resultado.setDescricao(rs.getString("DE_VIA"));
				resultado.setSigla(rs.getString("SG_VIA"));
				resultado.setSituacao(rs.getInt("ST_ATIVO"));	
				if (rs.getLong("CO_UF") > 0) {
					setUfs.add(new Uf(rs.getLong("CO_UF"), rs.getString("NO_UF")));						
				}
			}	
			if (resultado != null) {
				Set<Uf> setUfsOrdenado = new TreeSet<Uf>(setUfs);
				listUf.addAll(setUfsOrdenado);
				resultado.setUfs(listUf);
			}
		}catch(SQLException e){
			throw new RepositorioException(RepositorioException.ERRO_BD, e);
		}finally{
			Transacao.liberarRecursos(cs, rs);
		}
		return resultado;
	}
	
	public boolean verificaSiglaExiste(Rodovia bean) throws RepositorioException {
		Connection con = null;
		ResultSet rs = null;
		CallableStatement cs = null;
		boolean resultado = false;
		try {
			con = Transacao.getConnection();
			cs = con.prepareCall(VERIFICA_SIGLA);
			cs.setString(1, bean.getSigla());
			cs.registerOutParameter(2, OracleTypes.CURSOR);
			cs.execute();
			rs = (ResultSet) cs.getObject(2);
			if (rs.next()) {
				resultado = true;
			} else {
				resultado = false;
			}
		}catch(SQLException e){
			throw new RepositorioException(RepositorioException.ERRO_BD, e);
		}finally{
			Transacao.liberarRecursos(cs, rs);
		}
		return resultado;
	}
	
	public List<Rodovia> filtrarBEM(long idUf) throws RepositorioException {
		List<Rodovia> lista = new ArrayList<Rodovia>();
		Connection con = null;
		ResultSet rs = null;
		CallableStatement cs = null;
		try {
			con = Transacao.getConnection();
			cs = con.prepareCall(FILTRA_VIA_BEM);
			cs.setLong(1, idUf);
			cs.registerOutParameter(2, OracleTypes.CURSOR);
			cs.execute();
			rs = (ResultSet) cs.getObject(2);
			Rodovia resultado = null;
			while (rs.next()) {
				resultado = new Rodovia();
				resultado.setId(rs.getLong("CO_VIA"));
				resultado.setSigla(rs.getString("SG_VIA"));
				resultado.setNu_km_final_via(rs.getBigDecimal("nu_km_final_via"));
				lista.add(resultado);						
			}
		}catch(SQLException e){
			throw new RepositorioException(RepositorioException.ERRO_BD, e);
		}finally{
			Transacao.liberarRecursos(cs, rs);
		}
		return lista;
	}

}
