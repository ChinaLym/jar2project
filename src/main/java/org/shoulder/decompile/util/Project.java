package org.shoulder.decompile.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;

@Data
public class Project implements Comparable<Project>{

    private Project parent;
    private String groupId;
    private String artifactId;
    private String version;

    public void setGroupId(String groupId) {
        if(StrUtil.isBlank(this.groupId))
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        if(StrUtil.isBlank(this.artifactId))
        this.artifactId = artifactId;
    }

    public void setVersion(String version) {
        if(StrUtil.isBlank(this.version))
        this.version = version;
    }

    public static Project readFromXml(InputStream inputStream){
        Element document = XmlUtil.readXML(inputStream).getDocumentElement();
        Project project = new Project();
        project.setParent(new Project());
        NodeList groupIdList = document.getElementsByTagName("groupId");
        NodeList artifactIdList = document.getElementsByTagName("artifactId");
        NodeList versionList = document.getElementsByTagName("version");
        for (int i = 0; i < groupIdList.getLength(); i++) {
            Node current = groupIdList.item(i);
            String parentTagName = current.getParentNode().getNodeName();
            if("project".equals(parentTagName)){
                project.setGroupId(current.getTextContent());
            }else if("parent".equals(parentTagName)){
                project.getParent().setGroupId(current.getTextContent());
            }
        }
        for (int i = 0; i < artifactIdList.getLength(); i++) {
            Node current = artifactIdList.item(i);
            String parentTagName = current.getParentNode().getNodeName();
            if("project".equals(parentTagName)){
                project.setArtifactId(current.getTextContent());
            }else if("parent".equals(parentTagName)){
                project.getParent().setArtifactId(current.getTextContent());
            }
        }
        for (int i = 0; i < versionList.getLength(); i++) {
            Node current = versionList.item(i);
            String parentTagName = current.getParentNode().getNodeName();
            if("project".equals(parentTagName)){
                project.setVersion(current.getTextContent());
            }else if("parent".equals(parentTagName)){
                project.getParent().setVersion(current.getTextContent());
            }
        }

        if(StrUtil.isBlank(project.groupId)){
            project.groupId = project.parent.groupId;
        }

        if(StrUtil.isBlank(project.artifactId)){
            project.artifactId = project.parent.artifactId;
        }

        if(StrUtil.isBlank(project.version)){
            project.version = project.parent.version;
        }

        return project;
    }

    @Override
    public int compareTo(Project project) {
        if(project == null){
            return 1;
        }
        if(this.parent == null){
            return -1;
        }
        if(project.parent == null){
            return 1;
        }
        int result = 0;
        result = this.groupId.compareTo(project.groupId);
        if(result != 0){
            return result;
        }
        result = this.artifactId.compareTo(project.artifactId);
        if(result != 0){
            return result;
        }
        return this.version.compareTo(project.version);
    }

    @Override
    public String toString() {
        String str = "{";
                if(parent != null){
                    str += "parent=" + parent + ", \t\t";
                }

        str += "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
                return str;
    }
}