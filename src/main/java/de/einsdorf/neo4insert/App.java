package de.einsdorf.neo4insert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;

/**
 * Hello world!
 *
 */
public class App {
	private static final String FILE = "/home/gabriel/Documents/Data/HYPE/FieldOfStudyHierarchy.txt";
	private static final String[] NODE_TYPE = new String[] { "FieldOfStudy" };
	private static final String SUB_FIELD = "subfield";

	private Map<String, ReadNode> nodes = new HashMap<>();
	private List<ReadRelationship> relationships = new ArrayList<>();
	private Driver driver;

	public App() {
		driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "password"),
				Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig());

	}

	public static void main(String[] args) throws IOException {
		App app = new App();

		app.readDate(FILE);
		app.insertNodes();
		app.insertConnections();

	}

	private void insertConnections() {
		// TODO Auto-generated method stub
		
	}

	private void readDate(String file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				String[] elements = line.split("\t");
				String fieldID = elements[0];
				String nodelvl = elements[1];

				String parentFieldID = elements[2];
				String parentlvl = elements[3];

				double certainty = Double.parseDouble(elements[4]);

				boolean nodeKnown = nodes.containsKey(fieldID);
				boolean parentKnown = nodes.containsKey(parentFieldID);

				ReadNode node;
				ReadNode parent;
				if (nodeKnown) {
					node = nodes.get(fieldID);
				} else {
					node = addNewNode(nodelvl, fieldID);
				}
				if (parentKnown) {
					parent = nodes.get(parentFieldID);
				} else {
					// create new parent node
					parent = addNewNode(parentlvl, parentFieldID);
				}
				// create realationship
				createRelationship(node, parent, certainty);
			}
		}
	}

	public void insertNodes() {

		Session session = driver.session();

		// Delete all current nodes!
		session.run("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r");

		// TODO Also insert human readable name
		String insertionTemplate = "CREATE (node :FieldOfStudy {fieldID: {fieldID}, level:{nodelvl}})";

		// insert all nodes
		nodes.forEach((key, node) -> {
			session.run(insertionTemplate, Values.parameters("fieldID", key, "nodelvl", node.getNodelvl())).consume();
		});

		// create index
		String indexTemplate = "CREATE INDEX ON :FieldOfStudy(fieldID)";
		session.run(indexTemplate);
		session.close();

	}

	private ReadNode addNewNode(String nodelvl, String fieldID) {
		ReadNode node = new ReadNode(nodelvl, fieldID);
		nodes.put(fieldID, node);
		return node;
	}

	private void createRelationship(ReadNode node, ReadNode parent, double certainty) {
		relationships.add(new ReadRelationship(node, parent, certainty));
	}
}
