package io.tinyauth.elasticsearch.reflection;

import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Comparator;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.lang.reflect.ParameterizedType;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.Action;

import org.reflections.Reflections;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;


public class App {
  private static Comparator<Class<?>> classComparator = Comparator.comparing(c -> c.getCanonicalName());
  
  private static boolean hasMethod(Class<?> klass, String key, String returnType) {
    try {
      Method m = klass.getMethod(key);
      if (m == null) {
        return false;
      }

      if (!m.getGenericReturnType().getTypeName().equals(returnType)) {
        System.out.println("Method rejected due to return type: " + m.getGenericReturnType().getTypeName());
        return false;
      }

      return true;

    } catch (NoSuchMethodException e) {
      // System.out.println("NoSuchMethod exception thrown");
      return false;
    }
  }

  private static Class<?> getActionForRequest(String canonicalName) {
    if (canonicalName.endsWith("Request"))
        canonicalName = canonicalName.substring(0, canonicalName.length() - 7);

    if (canonicalName.endsWith("$"))
        canonicalName = canonicalName.substring(0, canonicalName.length() - 1);

    System.out.println(canonicalName);

    try {
      return Class.forName(canonicalName);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static void main(String[] args) {  
    Reflections reflections = new Reflections("org.elasticsearch");

    Set<Class<? extends ActionRequest>> requestTypes = reflections.getSubTypesOf(ActionRequest.class);
    requestTypes.stream().sorted(classComparator).forEach(t -> {
      System.out.println("import " + t.getCanonicalName() + ';');
    });
    
    System.out.println("\n\n");

    Set<Class<? extends Action>> actionTypes = reflections.getSubTypesOf(Action.class);
    actionTypes.stream().sorted(classComparator).forEach(actionType -> {
      String canonicalName = actionType.getCanonicalName();
      System.out.println(canonicalName);
      
      String permissionName;
      try {
        Field permissionField = actionType.getField("NAME");
        permissionName = (String)permissionField.get(null);
        if (permissionName == null) {
          System.out.println("/* Skipped " + actionType + " as it NAME seems to be null */");
          return;
        }
      } catch(NoSuchFieldException e) {
        System.out.println("/* Skipped " + actionType + " as it doesnt have a NAME */");
        return;
      } catch (IllegalAccessException e) {
        System.out.println("/* Skipped " + actionType + " as code generator not allowed to access NAME */");
        return;
      }
      
      System.out.println(permissionName);
      
      Method newRequestBuilder = Stream.of(actionType.getMethods())
        .filter(m -> !m.isBridge())
        .filter(m -> m.getName() == "newRequestBuilder")
        // .reduce((a, b) -> throw new RuntimeError("Got multiple newRequestBuilder!"))
        .collect(Collectors.toList()).get(0);

      Class<? extends ActionRequestBuilder> builderClass = (Class<? extends ActionRequestBuilder>)newRequestBuilder.getReturnType();
      System.out.println(builderClass);

      try {
        builderClass.getField("request");
      } catch (NoSuchFieldException e) {
        System.out.println("/* RequestBuilder does not have a request field */");
      }
      
      /*for (Field field : ) {
        System.out.format("Type: %s%n", field.getType());
        System.out.format("GenericType: %s%n", field.getGenericType());
      }*/
    
    /*Class<T> persistentClass = (Class<T>)
         ((ParameterizedType)getClass().getGenericSuperclass())
            .getActualTypeArguments()[0];*/

      System.out.println("**");
      Arrays.asList(actionType.getGenericInterfaces()).stream().forEach(t -> System.out.println(t));
      // ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericInterfaces()[0];

      System.out.println("**");
      Arrays.asList(actionType.getTypeParameters()).stream().forEach(t -> System.out.println(t));
      System.out.println("**");

      /*Stream.of(builderClass.getMethods())
        // .reduce((a, b) -> throw new RuntimeError("Got multiple newRequestBuilder!"))
        .forEach(m -> System.out.println(m));*/

      /*if (hasMethod(t, "indices", "java.lang.String[]")) {
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/indices.twig");
        JtwigModel model = JtwigModel.newModel().with("requestClassName", t.getName()).with("permissionName", "SomePermissionAction");
        template.render(model, System.out);
        return;
      }*/
      
      System.out.println("\n\n// Not able to generate wrapper for '" + actionType + "'\n\n");
    });
  }
}
