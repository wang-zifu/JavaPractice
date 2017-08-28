package shadows;
 
import java.util.List;
import java.util.Map;
 
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
 
import entities.entity;
import models.rawModel;
import models.texturedModel;
import renderEngine.masterRenderer;
import toolbox.math;
 
public class shadowMapEntityRenderer {
 
    private Matrix4f projectionViewMatrix;
    private shadowShader shader;
 
    protected shadowMapEntityRenderer(shadowShader shader, Matrix4f projectionViewMatrix) {
        this.shader = shader;
        this.projectionViewMatrix = projectionViewMatrix;
    }
 
    protected void render(Map<texturedModel, List<entity>> entities) {
        for (texturedModel model : entities.keySet()) {
            rawModel rawModel = model.getRawModel();
            bindModel(rawModel);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
            if(model.getTexture().isHasTransparency()) {
            	masterRenderer.disableCulling();
            }
            for (entity entity : entities.get(model)) {
                prepareInstance(entity);
                GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(),
                        GL11.GL_UNSIGNED_INT, 0);
            }
            if(model.getTexture().isHasTransparency()) {
            	masterRenderer.enableCulling();
            }
        }
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }
 
    private void bindModel(rawModel rawModel) {
        GL30.glBindVertexArray(rawModel.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        
    }

    private void prepareInstance(entity entity) {
        Matrix4f modelMatrix = math.createTransformationMatrix(entity.getPosition(),
                entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
        Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, modelMatrix, null);
        shader.loadMvpMatrix(mvpMatrix);
    }
 
}