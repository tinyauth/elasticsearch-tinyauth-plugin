package io.tinyauth.elasticsearch.reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Comparator;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
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
        System.out.println("// Method rejected due to return type: " + m.getGenericReturnType().getTypeName());
        return false;
      }

      return true;

    } catch (NoSuchMethodException e) {
      // System.out.println("NoSuchMethod exception thrown");
      return false;
    }
  }

  private static class Skip extends Exception {
    public Skip(Class<?> actionType, String reason) {
      super("Skipped " + actionType + " as " + reason);
    }
  }

  private static String getPermissionForAction(Class<? extends Action> actionType) throws Skip {
    String permissionName;
    try {
      Field permissionField = actionType.getField("NAME");
      permissionName = (String)permissionField.get(null);
      if (permissionName == null) {
        throw new Skip(actionType, "it's NAME seems to be null");
      }
    } catch(NoSuchFieldException e) {
      throw new Skip(actionType, "it doesn't have a NAME");
    } catch (IllegalAccessException e) {
      throw new Skip(actionType, "code generator not allowed to access NAME");
    }

    return Stream.of(permissionName.split(":"))
      .flatMap(part -> Stream.of(part.split("/")))
      .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
      .collect(Collectors.joining());
  }

  private static Class<? extends ActionRequest> getActionRequestForAction(Class<? extends Action> actionType) throws Skip {
    ParameterizedType actionTypeGeneric = (ParameterizedType)actionType.getGenericSuperclass();
    List<Class<?>> actionRequestTypes = Stream.of(actionTypeGeneric.getActualTypeArguments())
      .filter(t -> t instanceof Class<?>)
      .map(r -> (Class<?>)r)
      .filter(r -> ActionRequest.class.isAssignableFrom(r))
      .collect(Collectors.toList());

    if (actionRequestTypes.size() == 0) {
      throw new Skip(actionType, "could not find ActionRequest in actual type arguments for Action");
    } else if (actionRequestTypes.size() > 1) {
      throw new Skip(actionType, "found multiple ActionRequest in actual type arguments for Action");
    }

    return (Class<? extends ActionRequest>)actionRequestTypes.get(0);
  }

  public static String singleResource(String functionName, String resourceType) {
    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/singleResource.twig");
    JtwigModel model = JtwigModel.newModel()
      .with("functionName", functionName)
      .with("resourceType", resourceType);
    return template.render(model);
  }

  public static String listResource(String functionName, String resourceType) {
    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/listResource.twig");
    JtwigModel model = JtwigModel.newModel()
      .with("functionName", functionName)
      .with("resourceType", resourceType);
    return template.render(model);
  }

  public static String flatMapResource(String functionName, String resourceType) {
    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/flatMapResource.twig");
    JtwigModel model = JtwigModel.newModel()
      .with("functionName", functionName)
      .with("resourceType", resourceType);
    return template.render(model);
  }

  public static String indexRequest(String functionName, String resourceType) {
    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/indexRequest.twig");
    JtwigModel model = JtwigModel.newModel()
      .with("functionName", functionName)
      .with("resourceType", resourceType);
    return template.render(model);
  }

  public static String recursiveRequest(String functionName, String resourceType) {
    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/recursiveRequest.twig");
    JtwigModel model = JtwigModel.newModel()
      .with("functionName", functionName)
      .with("resourceType", resourceType);
    return template.render(model);
  }

  public static void main(String[] args) {
    Reflections reflections = new Reflections("org.elasticsearch");

    Set<Class<? extends ActionRequest>> allRequestTypes = reflections.getSubTypesOf(ActionRequest.class);
    allRequestTypes.stream().sorted(classComparator).forEach(t -> {
      System.out.println("import " + t.getCanonicalName() + ';');
    });

    System.out.println("\n\n");

    Set<Class<? extends Action>> actionTypes = reflections.getSubTypesOf(Action.class);
    actionTypes.stream().sorted(classComparator).forEach(actionType -> {
      try {
        String permissionName = getPermissionForAction(actionType);
        Class<? extends ActionRequest> actionRequestType = getActionRequestForAction(actionType);
        List<String> extractions = new ArrayList<String>();

        if (hasMethod(actionRequestType, "getIndex", "java.lang.String")) {
          extractions.add(singleResource("getIndex", "index"));
        }

        if (hasMethod(actionRequestType, "indices", "java.lang.String[]")) {
          extractions.add(listResource("indices", "index"));
        }

        if (hasMethod(actionRequestType, "getDestination", "org.elasticsearch.action.index.IndexRequest")) {
          extractions.add(indexRequest("getDestination", "index"));
        }

        if (hasMethod(actionRequestType, "nodeIds", "java.lang.String[]")) {
          extractions.add(listResource("nodeIds", "node"));
        }

        if (hasMethod(actionRequestType, "getNodes", "java.lang.String[]")) {
          extractions.add(listResource("getNodes", "node"));
        }

        if (hasMethod(actionRequestType, "getRequests", "java.lang.String[]")) {
          extractions.add(flatMapResource("getRequests", "index"));
        }

        if (hasMethod(actionRequestType, "requests", "java.lang.String[]")) {
          extractions.add(flatMapResource("requests", "index"));
        }

        if (actionRequestType.getSimpleName().contains("IndexTemplate")) {
          if (hasMethod(actionRequestType, "name", "java.lang.String")) {
            extractions.add(singleResource("name", "index-template"));
          }
          if (hasMethod(actionRequestType, "names", "java.lang.String[]")) {
            extractions.add(listResource("names", "index-template"));
          }
        }

        if (actionRequestType.getSimpleName().contains("Pipeline")) {
          if (hasMethod(actionRequestType, "getId", "java.lang.String")) {
            extractions.add(singleResource("getId", "pipeline"));
          }
          if (hasMethod(actionRequestType, "getIds", "java.lang.String[]")) {
            extractions.add(listResource("getIds", "pipeline"));
          }
        }

        if (actionRequestType.getSimpleName().contains("Repositor")) {
          if (hasMethod(actionRequestType, "name", "java.lang.String")) {
            extractions.add(singleResource("name", "repository"));
          }
        }

        if (hasMethod(actionRequestType, "repositories", "java.lang.String[]")) {
          extractions.add(listResource("repositories", "repository"));
        }

        if (actionRequestType.getSimpleName().contains("StoredScript")) {
          if (hasMethod(actionRequestType, "id", "java.lang.String")) {
            extractions.add(singleResource("id", "stored-script"));
          }
        }

        if (hasMethod(actionRequestType, "snapshot", "java.lang.String")) {
          extractions.add(singleResource("snapshot", "snapshot"));
        }

        if (hasMethod(actionRequestType, "snapshots", "java.lang.String[]")) {
          extractions.add(listResource("snapshots", "snapshot"));
        }

        if (hasMethod(actionRequestType, "requests", "java.util.List<org.elasticsearch.action.search.SearchRequest>")) {
          extractions.add(flatMapResource("requests", "index"));
        }

        if (hasMethod(actionRequestType, "getItems", "java.util.List<org.elasticsearch.action.get.MultiGetRequest$Item>")) {
          extractions.add(flatMapResource("getItems", "index"));
        }

        if (hasMethod(actionRequestType, "getRequests", "java.util.List<org.elasticsearch.action.termvectors.TermVectorsRequest>")) {
          extractions.add(flatMapResource("getRequests", "index"));
        }

        if (hasMethod(actionRequestType, "requests", "java.util.List<org.elasticsearch.action.DocWriteRequest>")) {
          extractions.add(recursiveRequest("requests", "index"));
        }

        if (extractions.size() == 0) {
          JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/simple.twig");
          JtwigModel model = JtwigModel.newModel()
            .with("requestClassName", actionRequestType.getSimpleName())
            .with("permissionName", permissionName);
          extractions.add(template.render(model));
        }

        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/body.twig");
        JtwigModel model = JtwigModel.newModel()
          .with("requestClassName", actionRequestType.getSimpleName())
          .with("permissionName", permissionName)
          .with("body", String.join("\n", extractions));
        template.render(model, System.out);

      } catch (Skip s) {
        System.out.println("/* " + s.getMessage() + " */");
      }

      System.out.println("");
    });
  }
}
