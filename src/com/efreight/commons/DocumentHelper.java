package com.efreight.commons;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import org.dom4j.Element;
import org.dom4j.Node;

public class DocumentHelper {
	
	public static void main(String[] args) {
		String s = "<fakeid>3660475</fakeid><nickname>大萝卜</nickname><wxid>weiweiluke</wxid><openid>ozBfxjkCdvQqFbnkXK3by4GsZjxk</openid>";
		try {
			Document doc = org.dom4j.DocumentHelper.parseText(s);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("111");
	}
	/**
	 * 查询某节点下的text
	 * @param xpath
	 * @return String
	 */
	public static String getNodeText(Document document , String xpath){
		if(document==null)
			return null;
		List<Element> list = document.selectNodes(xpath);
		String text = null;
		if(list!=null&&list.size()>0){
			Element target = list.get(0);
			if(target!=null&&target.getText()!=null&&!"".equals(target.getText())){
				text = target.getText();
			}
		}
		return text;
	}
	
	public static String getNodeText(Node node , String xpath){
		if(node==null)
			return null;
		Node subNode = node.selectSingleNode(xpath);
		String text =null;
		if(subNode!=null&&subNode.getText()!=null&&!"".equals(subNode.getText()))
			text = subNode.getText();
		return text;
	}
	
	
	public static List<Node> getNodeList(Document document , String xpath){
		List<Node> nodeList = document.selectNodes(xpath);
		return nodeList;
	}
	
	public static String asXML(Document document){
		String xml = document.asXML();
		return xml;
	}
}
