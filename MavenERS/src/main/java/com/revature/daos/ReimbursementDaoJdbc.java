package com.revature.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.revature.model.Reimbursement;
import com.revature.model.ReimbursementStatus;
import com.revature.model.ReimbursementType;
import com.revature.util.ConnectionUtil;

public class ReimbursementDaoJdbc implements ReimbursementDao {
	private Logger log = Logger.getRootLogger();

	public void addReimbursement(Reimbursement reimbursement) {
		try (Connection conn = ConnectionUtil.getConnection()) {
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO ers_reimbursement (reimb_amount,reimb_submitted,reimb_resolved,reimb_description,reimb_author,reimb_resolver,reimb_status_id,reimb_type_id)\r\n"
							+ "VALUES (?,?,?,?,?,?,?,?) RETURNING reimb_id;");

			ps.setDouble(1, reimbursement.getAmount());
			ps.setTimestamp(2, reimbursement.getSubmitted());
			ps.setNull(3, Types.TIMESTAMP);
			ps.setString(4, reimbursement.getDescription());
			ps.setInt(5, reimbursement.getAuthor());
			ps.setNull(6, Types.INTEGER);
			ps.setInt(7, reimbursement.getStatus().getId());
			ps.setInt(8, reimbursement.getType().getId());

			ResultSet rs = ps.executeQuery();

			int reimbursementId = -1;
			if (rs.next()) {
				reimbursementId = rs.getInt("reimb_id");
			} else {
				log.trace("Problem with reimbursement ID");
			}

			reimbursement.setReimbursementId(reimbursementId);

			log.trace(reimbursement);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Reimbursement> getReimbursements() {
		try (Connection conn = ConnectionUtil.getConnection()) {
			PreparedStatement ps = conn
					.prepareStatement("SELECT * FROM ers_reimbursement " + "INNER JOIN ers_reimbursement_status "
							+ "USING (reimb_status_id) " + "INNER JOIN ers_reimbursement_type "
							+ "USING (reimb_type_id) " + "ORDER BY reimb_status_id ASC;");

			ResultSet rs = ps.executeQuery();

			List<Reimbursement> reimbursementList = new ArrayList<>();
			while (rs.next()) {
				Integer i;
				if (rs.getInt("reimb_resolver") == 0) {
					i = null;
				} else {
					i = rs.getInt("reimb_resolver");
				}
				reimbursementList.add(new Reimbursement(
						rs.getInt("reimb_id"), 
						rs.getDouble("reimb_amount"),
						rs.getTimestamp("reimb_submitted"), 
						rs.getTimestamp("reimb_resolved"),
						rs.getString("reimb_description"), 
						rs.getInt("reimb_author"), 
						i,
						new ReimbursementStatus(rs.getInt("reimb_status_id"), rs.getString("reimb_status")),
						new ReimbursementType(rs.getInt("reimb_type_id"), rs.getString("reimb_type"))));
			}
			return reimbursementList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Reimbursement> getReimbursementByUser(int userId) {
		try (Connection conn = ConnectionUtil.getConnection()) {
			PreparedStatement ps = conn
					.prepareStatement("SELECT * FROM ers_reimbursement " + "INNER JOIN ers_reimbursement_status "
							+ "USING (reimb_status_id) " + "INNER JOIN ers_reimbursement_type "
							+ "USING (reimb_type_id) " + "WHERE reimb_author=? " + "ORDER BY reimb_status_id ASC;");

			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();

			List<Reimbursement> reimbursementList = new ArrayList<Reimbursement>();
			while (rs.next()) {
				reimbursementList.add(new Reimbursement(
						rs.getInt("reimb_id"), 
						rs.getDouble("reimb_amount"),
						rs.getTimestamp("reimb_submitted"), 
						rs.getTimestamp("reimb_resolved"),
						rs.getString("reimb_description"), 
						rs.getInt("reimb_author"), 
						rs.getInt("reimb_resolver"),
						new ReimbursementStatus(rs.getInt("reimb_status_id"), rs.getString("reimb_status")),
						new ReimbursementType(rs.getInt("reimb_type_id"), rs.getString("reimb_type"))));
			}
			return reimbursementList;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

}
