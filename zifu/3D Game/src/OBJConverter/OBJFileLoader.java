package OBJConverter;

	import java.io.BufferedReader;
	import java.io.File;
	import java.io.FileNotFoundException;
	import java.io.FileReader;
	import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
	import java.util.List;
	 
	import org.lwjgl.util.vector.Vector2f;
	import org.lwjgl.util.vector.Vector3f;
	 
	public class OBJFileLoader {
	     
	    private static final String RES_LOC = "res/";
	 
	    public static modelData loadOBJ(String objFileName) {
	    	InputStreamReader isr = new InputStreamReader(Class.class.getResourceAsStream("/res/" + objFileName + ".obj"));
	    	BufferedReader reader = new BufferedReader(isr);
	        String line;
	        List<vertex> vertices = new ArrayList<vertex>();
	        List<Vector2f> textures = new ArrayList<Vector2f>();
	        List<Vector3f> normals = new ArrayList<Vector3f>();
	        List<Integer> indices = new ArrayList<Integer>();
	        try {
	            while (true) {
	                line = reader.readLine();
	                if (line.startsWith("v ")) {
	                    String[] currentLine = line.split(" ");
	                    Vector3f vertex = new Vector3f((float) Float.valueOf(currentLine[1]),
	                            (float) Float.valueOf(currentLine[2]),
	                            (float) Float.valueOf(currentLine[3]));
	                    vertex newVertex = new vertex(vertices.size(), vertex);
	                    vertices.add(newVertex);
	 
	                } else if (line.startsWith("vt ")) {
	                    String[] currentLine = line.split(" ");
	                    Vector2f texture = new Vector2f((float) Float.valueOf(currentLine[1]),
	                            (float) Float.valueOf(currentLine[2]));
	                    textures.add(texture);
	                } else if (line.startsWith("vn ")) {
	                    String[] currentLine = line.split(" ");
	                    Vector3f normal = new Vector3f((float) Float.valueOf(currentLine[1]),
	                            (float) Float.valueOf(currentLine[2]),
	                            (float) Float.valueOf(currentLine[3]));
	                    normals.add(normal);
	                } else if (line.startsWith("f ")) {
	                    break;
	                }
	            }
	            while (line != null && line.startsWith("f ")) {
	                String[] currentLine = line.split(" ");
	                String[] vertex1 = currentLine[1].split("/");
	                String[] vertex2 = currentLine[2].split("/");
	                String[] vertex3 = currentLine[3].split("/");
	                processVertex(vertex1, vertices, indices);
	                processVertex(vertex2, vertices, indices);
	                processVertex(vertex3, vertices, indices);
	                line = reader.readLine();
	            }
	            reader.close();
	        } catch (IOException e) {
	            System.err.println("Error reading the file");
	        }
	        removeUnusedVertices(vertices);
	        float[] verticesArray = new float[vertices.size() * 3];
	        float[] texturesArray = new float[vertices.size() * 2];
	        float[] normalsArray = new float[vertices.size() * 3];
	        float furthest = convertDataToArrays(vertices, textures, normals, verticesArray,
	                texturesArray, normalsArray);
	        int[] indicesArray = convertIndicesListToArray(indices);
	        modelData data = new modelData(verticesArray, texturesArray, normalsArray, indicesArray,
	                furthest);
	        return data;
	    }
	 
	    private static void processVertex(String[] vertex, List<vertex> vertices, List<Integer> indices) {
	        int index = Integer.parseInt(vertex[0]) - 1;
	        vertex currentVertex = vertices.get(index);
	        int textureIndex = Integer.parseInt(vertex[1]) - 1;
	        int normalIndex = Integer.parseInt(vertex[2]) - 1;
	        if (!currentVertex.isSet()) {
	            currentVertex.setTextureIndex(textureIndex);
	            currentVertex.setNormalIndex(normalIndex);
	            indices.add(index);
	        } else {
	            dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices,
	                    vertices);
	        }
	    }
	 
	    private static int[] convertIndicesListToArray(List<Integer> indices) {
	        int[] indicesArray = new int[indices.size()];
	        for (int i = 0; i < indicesArray.length; i++) {
	            indicesArray[i] = indices.get(i);
	        }
	        return indicesArray;
	    }
	 
	    private static float convertDataToArrays(List<vertex> vertices, List<Vector2f> textures,
	            List<Vector3f> normals, float[] verticesArray, float[] texturesArray,
	            float[] normalsArray) {
	        float furthestPoint = 0;
	        for (int i = 0; i < vertices.size(); i++) {
	            vertex currentVertex = vertices.get(i);
	            if (currentVertex.getLength() > furthestPoint) {
	                furthestPoint = currentVertex.getLength();
	            }
	            Vector3f position = currentVertex.getPosition();
	            Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
	            Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
	            verticesArray[i * 3] = position.x;
	            verticesArray[i * 3 + 1] = position.y;
	            verticesArray[i * 3 + 2] = position.z;
	            texturesArray[i * 2] = textureCoord.x;
	            texturesArray[i * 2 + 1] = 1 - textureCoord.y;
	            normalsArray[i * 3] = normalVector.x;
	            normalsArray[i * 3 + 1] = normalVector.y;
	            normalsArray[i * 3 + 2] = normalVector.z;
	        }
	        return furthestPoint;
	    }
	 
	    private static void dealWithAlreadyProcessedVertex(vertex previousVertex, int newTextureIndex,
	            int newNormalIndex, List<Integer> indices, List<vertex> vertices) {
	        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
	            indices.add(previousVertex.getIndex());
	        } else {
	            vertex anotherVertex = previousVertex.getDuplicateVertex();
	            if (anotherVertex != null) {
	                dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex,
	                        indices, vertices);
	            } else {
	                vertex duplicateVertex = new vertex(vertices.size(), previousVertex.getPosition());
	                duplicateVertex.setTextureIndex(newTextureIndex);
	                duplicateVertex.setNormalIndex(newNormalIndex);
	                previousVertex.setDuplicateVertex(duplicateVertex);
	                vertices.add(duplicateVertex);
	                indices.add(duplicateVertex.getIndex());
	            }
	 
	        }
	    }
	     
	    private static void removeUnusedVertices(List<vertex> vertices){
	        for(vertex vertex:vertices){
	            if(!vertex.isSet()){
	                vertex.setTextureIndex(0);
	                vertex.setNormalIndex(0);
	            }
	        }
	    }
	}