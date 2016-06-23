package de.einsdorf.neo4insert;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.innerloop.neo4j.client.Connection;
import io.innerloop.neo4j.client.GraphStatement;
import io.innerloop.neo4j.client.Neo4jClient;
import io.innerloop.neo4j.client.Node;
import io.innerloop.neo4j.client.Relationship;
import io.innerloop.neo4j.client.Statement;

/**
 * Hello world!
 *
 */
public class App {
	private static final String FILE = "/home/gabriel/Documents/Data/HYPE/FieldOfStudyHierarchy.txt";
	private static final String[] NODE_TYPE = new String[] { "FieldOfStudy" };
	private static final String SUB_FIELD = "subfield";
	private Neo4jClient client;
	private Map<String, Node> nodes = new HashMap<>();
	private List<Relationship> relationships = new ArrayList<>();

	public App() {
		client = new Neo4jClient("http://localhost:7474/db/data", "neo4j", "password");
	}

	public static void main(String[] args) throws IOException {
		App app = new App();

		app.insert(FILE);

	}

	private void insert(String file2) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file2));
		Connection connection = client.getConnection();

		long relIDCounter = 0;
		long nodeIdCounter = 0;

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

			Node node;
			Node parent;
			if (nodeKnown) {
				node = nodes.get(fieldID);
			} else {
				nodeIdCounter++;
				node = addNewNode(nodeIdCounter, nodelvl, fieldID);
			}
			if (parentKnown) {
				parent = nodes.get(parentFieldID);
			} else {
				// create new parent node
				nodeIdCounter++;
				parent = addNewNode(nodeIdCounter, parentlvl, parentFieldID);
			}
			// create realationship
			relIDCounter++;
			createRelationship(relIDCounter, certainty, node, parent);
		}
		System.out.println("");

		nodes.forEach((key, node) -> {
			GraphStatement g = new GraphStatement("CREATE (a:FielOfStudy {0})");
		});

	}

	private Node addNewNode(long nodeIdCounter, String nodelvl, String fieldID) {
		Map<String, Object> nodeProps = new HashMap<>();
		nodeProps.put("Level", nodelvl);
		nodeProps.put("FieldID", fieldID);
		Node parent = new Node(nodeIdCounter, NODE_TYPE, nodeProps);
		nodes.put(fieldID, parent);
		return parent;
	}

	private void createRelationship(long relID, double certainty, Node node, Node parent) {
		Map<String, Object> relprops = new HashMap<>();
		relprops.put("certainty", certainty);
		relationships.add(new Relationship(relID, SUB_FIELD, node.getId(), parent.getId(), relprops));
	}
}
