package com.babeeta.butterfly.account.dao;

import java.net.UnknownHostException;

import com.babeeta.butterfly.account.entity.Account;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class AccountDaoT {
	public static void main(String[] args) {
		/*
		 * Datastore ds = ...; // like new Morphia(new
		 * Mongo()).createDatastore("hr") morphia.map(Employee.class);
		 * 
		 * ds.save(new Employee("Mister", "GOD", null, 0)); // get an employee
		 * without a manager Employee boss =
		 * ds.find(Employee.class).field("manager").equal(null).get();
		 * 
		 * Key<Employee> scottsKey = ds.save(new Employee("Scott", "Hernandez",
		 * ds.getKey(boss), 150*1000));
		 * 
		 * //add Scott as an employee of his manager ds.update(boss,
		 * ds.createUpdateOperations(Employee.class).add("underlings",
		 * scottsKey)); // get Scott's boss; the same as the one above. Employee
		 * scottsBoss = ds.find(Employee.class).filter("underlings",
		 * scottsKey).get();
		 * 
		 * for (Employee e : ds.find(Employee.class, "manager", boss)) print(e);
		 */

		Morphia morphia = new Morphia();
		Datastore ds = null;
		try {
			ds = morphia.createDatastore(new Mongo("mongodb"), "testMorphia");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		morphia.map(com.babeeta.butterfly.account.entity.Account.class);
		/*
		 * Account account = new Account(); account.setId("1");
		 * account.setSecureKey("1"); account.setUserId("1"); Map map = new
		 * HashMap<String, Object>(); map.put("os", "microsoft-windows7");
		 * account.setExtra(map); ds.save(account);
		 */
		/*
		 * List<Account> list = ds.createQuery(Account.class) .filter("userId",
		 * "2").filter("status", "NORMAL").asList();
		 * 
		 * for (int i = 0; i < list.size(); i++) { ObjectMapper mapper = new
		 * ObjectMapper(); try {
		 * System.out.println(mapper.writeValueAsString(account)); } catch
		 * (JsonGenerationException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } catch (JsonMappingException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } catch (IOException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); } }
		 */
		// Iterator<String> it = map.keySet().iterator();
		/*
		 * Query<Account> query = ds.createQuery(Account.class).filter( "_id",
		 * "1e0331dec9cb4e71be0cb416a3df3297"); UpdateOperations<Account> ops =
		 * ds .createUpdateOperations(Account.class); ops.set("userId", "3");
		 * ds.update(query, ops);
		 */
		Account account = new Account();
		account.setId("1e0331dec9cb4e71be0cb416a3df3297");
		ds.delete(account);
	}
}
