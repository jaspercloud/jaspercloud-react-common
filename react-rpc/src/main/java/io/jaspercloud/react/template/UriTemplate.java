package io.jaspercloud.react.template;

import io.jaspercloud.exception.ReactRpcException;

import java.util.ArrayList;
import java.util.List;

public class UriTemplate {

    private static final int Init = 1;
    private static final int Left = 2;

    private List<Node> nodeList = new ArrayList<>();

    public List<Node> getNodeList() {
        return nodeList;
    }

    public UriTemplate(String template) {
        parse(template);
    }

    private void parse(String template) {
        char[] chars = template.toCharArray();
        List<UriTemplate.Node> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        int status = Init;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '{':
                    if (Init != status) {
                        throw new ReactRpcException(String.format("not found }: %s", builder.toString()));
                    }
                    list.add(new UriTemplate.StringNode(builder.toString()));
                    builder = new StringBuilder();
                    status = Left;
                    break;
                case '}':
                    if (Left != status) {
                        throw new ReactRpcException(String.format("not found {: %s", builder.toString()));
                    }
                    list.add(new UriTemplate.ExpressionNode(builder.toString()));
                    builder = new StringBuilder();
                    status = Init;
                    break;
                default:
                    builder.append(c);
                    break;
            }
        }
        if (builder.length() > 0) {
            if (Init != status) {
                throw new ReactRpcException(String.format("not found }: %s", builder.toString()));
            } else {
                list.add(new StringNode(builder.toString()));
            }
        }
        nodeList = list;
    }

    public static class Node {

    }

    public static class StringNode extends Node {

        private String template;

        public String getTemplate() {
            return template;
        }

        public StringNode(String template) {
            this.template = template;
        }
    }

    public static class ExpressionNode extends Node {

        private String name;

        public String getName() {
            return name;
        }

        public ExpressionNode(String name) {
            this.name = name;
        }
    }
}
