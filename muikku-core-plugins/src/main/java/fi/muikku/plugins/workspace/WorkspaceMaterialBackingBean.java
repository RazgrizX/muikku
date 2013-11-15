package fi.muikku.plugins.workspace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;
import com.ocpsoft.pretty.faces.annotation.URLQueryParameter;

import fi.muikku.model.workspace.WorkspaceEntity;
import fi.muikku.plugins.material.model.Material;
import fi.muikku.plugins.workspace.model.WorkspaceMaterial;
import fi.muikku.schooldata.WorkspaceController;

@SuppressWarnings("el-syntax")
@Named
@Stateful
@RequestScoped
@URLMappings(mappings = { 
  @URLMapping(
	  id = "workspace-material", 
  	pattern = "/workspace/#{workspaceMaterialBackingBean.workspaceUrlName}/materials/#{ /[a-zA-Z0-9_\\/\\.\\\\-][a-zA-Z0-9_\\/\\.\\\\-]*/ workspaceMaterialBackingBean.workspaceMaterialPath}", 
  	viewId = "/workspaces/workspace-material.jsf"
  )}
)
public class WorkspaceMaterialBackingBean {
	
	@Inject
	private WorkspaceController workspaceController;
	
	@Inject
	private WorkspaceMaterialController workspaceMaterialController;
	
	@URLAction 
	public void init() throws IOException {
	  if (StringUtils.isBlank(getWorkspaceUrlName())) {
			throw new FileNotFoundException();
		}
		
		WorkspaceEntity workspaceEntity = workspaceController.findWorkspaceEntityByUrlName(getWorkspaceUrlName());

		if (workspaceEntity == null) {
			throw new FileNotFoundException();
		}
		
		WorkspaceMaterial workspaceMaterial = workspaceMaterialController.findWorkspaceMaterialByWorkspaceEntityAndPath(workspaceEntity, getWorkspaceMaterialPath());
		if (workspaceMaterial == null) {
			throw new FileNotFoundException();
		}

	  Material material = workspaceMaterial.getMaterial();
	  if (material == null) {
	  	throw new FileNotFoundException();
	  }
	  
	  if (Boolean.TRUE == getEmbed()) {
	  	FacesContext.getCurrentInstance().getExternalContext().redirect(new StringBuilder()
        .append(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath())
        .append("/workspace/")
        .append(workspaceEntity.getUrlName())
        .append("/materials/binary/")
        .append(workspaceMaterial.getPath())
        .toString());
	  }
	  
	  materialId = material.getId();
	  materialType = material.getType();
	}
	
	public Boolean getEmbed() {
		return embed;
	}
	
	public void setEmbed(Boolean embed) {
		this.embed = embed;
	}
	
	public Long getMaterialId() {
		return materialId;
	}
	
	public void setMaterialId(Long materialId) {
    this.materialId = materialId;
  }

	public String getMaterialType() {
		return materialType;
	}
	
	public void setMaterialType(String materialType) {
    this.materialType = materialType;
  }
	
	public Long getWorkspaceMaterialId() {
		return workspaceMaterialId;
	}
	
	public void setWorkspaceMaterialId(Long workspaceMaterialId) {
		this.workspaceMaterialId = workspaceMaterialId;
	}

	public String getWorkspaceMaterialPath() {
		return workspaceMaterialPath;
	}
	
	public void setWorkspaceMaterialPath(String workspaceMaterialPath) {
		this.workspaceMaterialPath = workspaceMaterialPath;
	}
	
	public String getWorkspaceUrlName() {
		return workspaceUrlName;
	}

	public void setWorkspaceUrlName(String workspaceUrlName) {
		this.workspaceUrlName = workspaceUrlName;
	}
	
	public void save() {
	  String queryFieldPrefix = "material-form:queryform:";
	  
	  Map<String, String> values = new HashMap<>();
	  Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
	  
	  Iterator<String> parameterNames = requestParameterMap.keySet().iterator();
	  while (parameterNames.hasNext()) {
	    String parameterName = parameterNames.next();
	    if (StringUtils.startsWith(parameterName, queryFieldPrefix)) {
	      String value = requestParameterMap.get(parameterName);
	      values.put(StringUtils.removeStart(parameterName, queryFieldPrefix), value);
	    }
	  }
	  
	  for (String key : values.keySet()) {
	    String value = values.get(key);
	    
	    System.out.println(key + " == " + value);
	  }
	}

	@URLQueryParameter ("embed")
	private Boolean embed;
	private Long workspaceMaterialId;
	private String workspaceMaterialPath;
	private Long materialId;
	private String materialType;
	private String workspaceUrlName;
}