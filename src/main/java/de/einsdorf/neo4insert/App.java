package de.einsdorf.neo4insert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {
	private static final String FILE = "/home/gabriel/Documents/Data/HYPE/combinedHierachicalFields.csv";

	private static final Logger LOG = LoggerFactory.getLogger(App.class);

	private Map<String, ReadNode> nodes = new HashMap<>();
	private List<ReadRelationship> relationships = new ArrayList<>();
	private Driver driver;

	public App() {
		driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "password"),
				Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig());

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		App app = new App();

		app.readData(FILE);
		app.insertNodes();
		app.insertConnections();

	}

	private void insertConnections() {

		LOG.warn("> inserting {} conections", relationships.size());
		Session session = driver.session();
		String queryTemplate = "MATCH (node:FieldOfStudy {fieldID: {nodeID}}),"
				+ "(parent:FieldOfStudy {fieldID: {parentID}})"
				+ "CREATE (node) - [c:subField {certainty: {cert}}] -> (parent)" + "RETURN c";

		relationships.forEach(rel -> {
			final String fieldID = rel.getNode().getFieldID();
			final String parentID = rel.getParent().getFieldID();
			final double certainty = rel.getCertainty();

			StatementResult out = session.run(queryTemplate,
					Values.parameters("nodeID", fieldID, "parentID", parentID, "cert", certainty));
			out.consume();
		});
		LOG.warn("< inserted {} conections", relationships.size());

	}

	private void readData(String file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			int count = 0;
			LOG.warn("> reading input file");
			while (true) {

				String line = reader.readLine();
				count++;

				if (line == null) {
					break;
				}
				String[] elements = line.split("\t");
				String fieldID = elements[0];
				String nodelvl = elements[1];
				String fieldName = elements[2];

				String parentFieldID = elements[3];
				String parentlvl = elements[4];
				String parentName = elements[5];

				double certainty = Double.parseDouble(elements[6]);

				boolean nodeKnown = nodes.containsKey(fieldID);
				boolean parentKnown = nodes.containsKey(parentFieldID);

				ReadNode node;
				ReadNode parent;
				if (nodeKnown) {
					node = nodes.get(fieldID);
				} else {
					node = addNewNode(nodelvl, fieldID, fieldName);
				}
				if (parentKnown) {
					parent = nodes.get(parentFieldID);
				} else {
					// create new parent node
					parent = addNewNode(parentlvl, parentFieldID, parentName);
				}
				// create realationship
				createRelationship(node, parent, certainty);
			}
			LOG.warn("< read {} lines", count);
		}
	}

	public void insertNodes() throws InterruptedException {
		Session session = driver.session();
		LOG.warn("cleaning up the database");

		// Delete all current nodes!
		session.run("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r");

		// insert all nodes
		LOG.warn("> Inserting {} nodes.", nodes.size());
		String insertionTemplate = "CREATE (node :FieldOfStudy {fieldID: {fieldID}, level:{nodelvl}, fieldName:{fName}})";

		nodes.forEach((key, node) -> {
			session.run(insertionTemplate,
					Values.parameters("fieldID", key, "nodelvl", node.getNodelvl(), "fName", node.getFieldName()))
					.consume();
		});
		LOG.warn("< Inserted {} nodes.", nodes.size());

		LOG.warn("creating index");
		// create index
		String indexTemplate = "CREATE INDEX ON :FieldOfStudy(fieldID)";
		session.run(indexTemplate);
		session.close();

		long seconds = 20;
		LOG.warn("sleeping {} seconds to allow for the index to be populated", seconds);
		Thread.sleep(seconds * 1000L);
		LOG.warn("< index hopefully created");
	}

	private ReadNode addNewNode(String nodelvl, String fieldID, String fieldName) {
		ReadNode node = new ReadNode(nodelvl, fieldID, fieldName);
		nodes.put(fieldID, node);
		return node;
	}

	private void createRelationship(ReadNode node, ReadNode parent, double certainty) {
		relationships.add(new ReadRelationship(node, parent, certainty));
	}
}
