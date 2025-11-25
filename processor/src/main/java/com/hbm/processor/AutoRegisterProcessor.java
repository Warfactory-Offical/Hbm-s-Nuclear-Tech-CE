package com.hbm.processor;

import com.google.auto.service.AutoService;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.AutoRegisterContainer;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "com.hbm.interfaces.AutoRegister",
        "com.hbm.interfaces.AutoRegisterContainer"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoRegisterProcessor extends AbstractProcessor {

    private static final String TESR_FQN = "net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer";
    private static final String TE_FQN = "net.minecraft.tileentity.TileEntity";
    private static final String ENTITY_FQN = "net.minecraft.entity.Entity";
    private static final String ICONFIGURABLE_FQN = "com.hbm.tileentity.IConfigurableMachine";
    private static final String RENDER_FQN = "net.minecraft.client.renderer.entity.Render";
    private static final String TEISR_FQN = "net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer";
    private static final String DEFAULT_CLASS_FQN = "java.lang.Object";

    private final List<EntityInfo> entities = new ArrayList<>();
    private final Map<String, String> tileEntities = new LinkedHashMap<>();
    private final List<EntityRendererInfo> entityRenderers = new ArrayList<>();
    private final Map<String, String> tileEntityRenderers = new LinkedHashMap<>();
    private final List<TeisrInfo> itemRenderers = new ArrayList<>();
    private final List<String> configurableMachines = new ArrayList<>();

    private Filer filer;
    private Messager messager;
    private Types typeUtils;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<Element> annotatedElements = new HashSet<>();
        annotatedElements.addAll(roundEnv.getElementsAnnotatedWith(AutoRegister.class));
        annotatedElements.addAll(roundEnv.getElementsAnnotatedWith(AutoRegisterContainer.class));
        for (Element element : annotatedElements) {
            if (!(element instanceof TypeElement)) continue;
            TypeElement typeElement = (TypeElement) element;
            if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) continue;

            for (AutoRegister annotation : typeElement.getAnnotationsByType(AutoRegister.class)) {
                try {
                    processAnnotation(typeElement, annotation);
                } catch (Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Could not process @AutoRegister: " + e.getMessage(), element);
                }
            }
        }
        if (roundEnv.processingOver() && (!entities.isEmpty() || !tileEntities.isEmpty() || !entityRenderers.isEmpty() || !tileEntityRenderers.isEmpty() || !itemRenderers.isEmpty())) {
            try {
                generateRegistrarFile();
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to generate registrar file: " + e.getMessage());
            }
        }
        return true;
    }

    private void processAnnotation(TypeElement annotatedElement, AutoRegister annotation) {
        String annotatedFqn = annotatedElement.getQualifiedName().toString();

        if (isSubtypeByString(annotatedElement, TESR_FQN)) {
            String tileEntityClassName = getClassNameFromAnnotation(annotation, "tileentity");
            if (tileEntityClassName.equals(DEFAULT_CLASS_FQN)) {
                tileEntityClassName = getGenericSupertypeFqn(annotatedElement, TESR_FQN);

                if (tileEntityClassName == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Could not infer TileEntity type for renderer. Please specify it manually using " +
                            "'tileentity = ...'.", annotatedElement);
                    return;
                }
            }
            tileEntityRenderers.put(tileEntityClassName, annotatedFqn);

        } else if (isSubtypeByString(annotatedElement, RENDER_FQN)) {
            String entityClassName = getClassNameFromAnnotation(annotation, "entity");
            if (entityClassName.equals(DEFAULT_CLASS_FQN)) {
                entityClassName = getGenericSupertypeFqn(annotatedElement, RENDER_FQN);

                if (entityClassName == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Could not infer Entity type for renderer. Please specify it manually using " +
                            "'entity = ...'.", annotatedElement);
                    return;
                }
            }
            String factoryFieldName = annotation.factory();
            entityRenderers.add(new EntityRendererInfo(entityClassName, annotatedFqn, factoryFieldName));
        } else if (isSubtypeOf(annotatedElement, TEISR_FQN)) {
            if (annotation.item().isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "A TEISR class must specify the 'item' parameter in its @AutoRegister annotation.",
                        annotatedElement);
                return;
            }
            boolean hasArrayArgs = annotation.constructorArgs().length > 0;
            boolean hasStringArgs = !annotation.constructorArgsString().isEmpty();
            boolean hasInstanceField = !annotation.instanceField().isEmpty();

            if (hasArrayArgs && hasStringArgs) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Cannot use both 'constructorArgs' and 'constructorArgsString'. Please use only one.", annotatedElement);
                return;
            }
            if (hasInstanceField && (hasArrayArgs || hasStringArgs)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Cannot use constructor args when 'instanceField' is set. Please use only one approach.", annotatedElement);
                return;
            }

            String finalArgs;
            if (hasStringArgs) {
                finalArgs = annotation.constructorArgsString();
            } else {
                finalArgs = String.join(", ", annotation.constructorArgs());
            }
            itemRenderers.add(new TeisrInfo(annotatedFqn, annotation.item(), finalArgs, annotation.instanceField()));

        } else if (isSubtypeOf(annotatedElement, TE_FQN)) {
            String regId = annotation.name().trim().isEmpty() ? generateRegistrationId(annotatedElement.getSimpleName().toString()) :
                    annotation.name();
            tileEntities.put(annotatedFqn, regId);
            if (isSubtypeOf(annotatedElement, ICONFIGURABLE_FQN)) {
                configurableMachines.add(annotatedFqn);
            }

        } else if (isSubtypeOf(annotatedElement, ENTITY_FQN)) {
            if (annotation.name().trim().isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Entity registration requires a non-empty 'name' parameter.", annotatedElement);
                return;
            }
            entities.add(new EntityInfo(annotatedFqn, annotation.name(), annotation.trackingRange(), annotation.updateFrequency(),
                    annotation.sendVelocityUpdates(), annotation.eggColors()));

        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "Class is not a valid type for @AutoRegister. Must extend Entity, TileEntity, or a valid " +
                    "Renderer class.", annotatedElement);
        }
    }

    private boolean isSubtypeOf(TypeElement element, String targetSuperclassFqn) {
        TypeElement superclassElement = elementUtils.getTypeElement(targetSuperclassFqn);
        if (superclassElement == null) {
            return false;
        }
        return typeUtils.isSubtype(element.asType(), superclassElement.asType());
    }

    private void generateRegistrarFile() throws IOException {
        ClassName ENTITY_REGISTRY = ClassName.get("net.minecraftforge.fml.common.registry", "EntityRegistry");
        ClassName GAME_REGISTRY = ClassName.get("net.minecraftforge.fml.common.registry", "GameRegistry");
        ClassName CLIENT_REGISTRY = ClassName.get("net.minecraftforge.fml.client.registry", "ClientRegistry");
        ClassName RENDERING_REGISTRY = ClassName.get("net.minecraftforge.fml.client.registry", "RenderingRegistry");
        ClassName RESOURCE_LOCATION = ClassName.get("net.minecraft.util", "ResourceLocation");
        ClassName SIDE_ONLY = ClassName.get("net.minecraftforge.fml.relauncher", "SideOnly");
        ClassName SIDE = ClassName.get("net.minecraftforge.fml.relauncher", "Side");
        ClassName REF_STRINGS = ClassName.get("com.hbm", "Tags");
        ClassName MAIN_REGISTRY = ClassName.get("com.hbm.main", "MainRegistry");
        ClassName MOD_ITEMS = ClassName.get("com.hbm.items", "ModItems");
        ClassName ICONFIGURABLE_MACHINE = ClassName.get("com.hbm.tileentity", "IConfigurableMachine");
        TypeSpec.Builder registrarBuilder =
                TypeSpec.classBuilder("GeneratedHBMRegistrar").addModifiers(Modifier.PUBLIC, Modifier.FINAL).addJavadoc("AUTO-GENERATED FILE. DO NOT MODIFY.");
        if (!configurableMachines.isEmpty()) {
            TypeName classOfConfigurable = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ICONFIGURABLE_MACHINE));
            TypeName listOfConfigurables = ParameterizedTypeName.get(ClassName.get(java.util.List.class), classOfConfigurable);
            registrarBuilder.addField(FieldSpec.builder(listOfConfigurables, "CONFIGURABLE_MACHINES").addModifiers(Modifier.PUBLIC, Modifier.STATIC
                    , Modifier.FINAL).initializer("new $T<>()", java.util.ArrayList.class).build());
            CodeBlock.Builder staticBlock = CodeBlock.builder();
            for (String fqn : configurableMachines) {
                staticBlock.addStatement("$N.add($T.class)", "CONFIGURABLE_MACHINES", ClassName.bestGuess(fqn));
            }
            registrarBuilder.addStaticBlock(staticBlock.build());
        }
        if (!entities.isEmpty()) {
            MethodSpec.Builder method =
                    MethodSpec.methodBuilder("registerEntities").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(int.class).addParameter(int.class, "startId").addJavadoc("@param startId The starting ID for entity registration.\n@return The next available entity ID.");
            method.addStatement("int currentId = startId");
            for (EntityInfo info : entities) {
                method.addStatement("$T.registerModEntity(new $T($T.MODID, $S), $T.class, $S, currentId++, $T.instance, $L, $L, $L)",
                        ENTITY_REGISTRY, RESOURCE_LOCATION, REF_STRINGS, info.name, ClassName.bestGuess(info.fqn), info.name, MAIN_REGISTRY,
                        info.trackingRange, info.updateFrequency, info.sendVelocityUpdates);
            }
            for(EntityInfo info : entities) {
                if(info.eggColors[0] + info.eggColors[1] != 0x0) {
                    method.addStatement("$T.registerEgg(new $T($T.MODID, $S), $L, $L)", ENTITY_REGISTRY, RESOURCE_LOCATION, REF_STRINGS, info.name, info.eggColors[0], info.eggColors[1]);
                }
            }
            method.addStatement("return currentId");
            registrarBuilder.addMethod(method.build());
        }
        if (!tileEntities.isEmpty()) {
            MethodSpec.Builder method = MethodSpec.methodBuilder("registerTileEntities").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
            for (Map.Entry<String, String> entry : tileEntities.entrySet()) {
                method.addStatement("$T.registerTileEntity($T.class, new $T($T.MODID, $S))", GAME_REGISTRY, ClassName.bestGuess(entry.getKey()),
                        RESOURCE_LOCATION, REF_STRINGS, entry.getValue());
            }
            registrarBuilder.addMethod(method.build());
        }
        if (!entityRenderers.isEmpty()) {
            MethodSpec.Builder method =
                    MethodSpec.methodBuilder("registerEntityRenderers").addModifiers(Modifier.PUBLIC, Modifier.STATIC).addAnnotation(AnnotationSpec.builder(SIDE_ONLY).addMember("value", "$T.CLIENT", SIDE).build());
            for (EntityRendererInfo info : entityRenderers) {
                if (info.factoryFieldName.isEmpty()) {
                    method.addStatement("$T.registerEntityRenderingHandler($T.class, $T::new)", RENDERING_REGISTRY,
                            ClassName.bestGuess(info.entityFqn), ClassName.bestGuess(info.rendererFqn));
                } else {
                    method.addStatement("$T.registerEntityRenderingHandler($T.class, $T.$L)", RENDERING_REGISTRY,
                            ClassName.bestGuess(info.entityFqn), ClassName.bestGuess(info.rendererFqn), info.factoryFieldName);
                }
            }
            registrarBuilder.addMethod(method.build());
        }
        if (!tileEntityRenderers.isEmpty()) {
            MethodSpec.Builder method =
                    MethodSpec.methodBuilder("registerTileEntityRenderers").addModifiers(Modifier.PUBLIC, Modifier.STATIC).addAnnotation(AnnotationSpec.builder(SIDE_ONLY).addMember("value", "$T.CLIENT", SIDE).build());
            for (Map.Entry<String, String> entry : tileEntityRenderers.entrySet()) {
                method.addStatement("$T.bindTileEntitySpecialRenderer($T.class, new $T())",
                        CLIENT_REGISTRY, ClassName.bestGuess(entry.getKey()), ClassName.bestGuess(entry.getValue()));
            }
            registrarBuilder.addMethod(method.build());
        }
        if (!itemRenderers.isEmpty()) {
            MethodSpec.Builder method = MethodSpec.methodBuilder("registerItemRenderers")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addAnnotation(AnnotationSpec.builder(SIDE_ONLY).addMember("value", "$T.CLIENT", SIDE).build());
            for (TeisrInfo info : itemRenderers) {
                if (info.instanceFieldName.isEmpty()) {
                    method.addStatement("$T.$L.setTileEntityItemStackRenderer(new $T(" + info.constructorArgs + "))",
                            MOD_ITEMS, info.itemFieldName, ClassName.bestGuess(info.rendererFqn));
                } else {
                    method.addStatement("$T.$L.setTileEntityItemStackRenderer($T.$L)",
                            MOD_ITEMS, info.itemFieldName, ClassName.bestGuess(info.rendererFqn), info.instanceFieldName);
                }
            }
            registrarBuilder.addMethod(method.build());
        }
        JavaFile.builder("com.hbm.generated", registrarBuilder.build()).addFileComment("AUTO-GENERATED FILE. DO NOT MODIFY.").indent("    ").build().writeTo(filer);
    }

    private String generateRegistrationId(String name) {
        name = name.replaceFirst("^TileEntity", "");
        return "tileentity_" + name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private String getClassNameFromAnnotation(AutoRegister annotation, String methodName) {
        try {
            switch (methodName) {
                case "entity":
                    annotation.entity();
                    break;
                case "tileentity":
                    annotation.tileentity();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid method: " + methodName);
            }
        } catch (MirroredTypeException mte) {
            return ((TypeElement) typeUtils.asElement(mte.getTypeMirror())).getQualifiedName().toString();
        }
        throw new IllegalStateException("Failed to get class name for " + methodName + ". This is an annotation processor bug.");
    }

    /**
     * Retarded hack to deal with parameterized classes because apparently isSubtypeOf don't like them
     */
    private static boolean isSubtypeByString(TypeElement element, String targetFqn) {
        if (element == null) {
            return false;
        }

        Queue<TypeMirror> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(element.asType());

        while (!queue.isEmpty()) {
            TypeMirror currentType = queue.poll();
            if (!(currentType instanceof DeclaredType)) {
                continue;
            }
            TypeElement currentElement = (TypeElement) ((DeclaredType) currentType).asElement();
            String currentFqn = currentElement.getQualifiedName().toString();
            if (!visited.add(currentFqn)) {
                continue;
            }
            if (currentFqn.equals(targetFqn)) {
                return true;
            }
            TypeMirror superclass = currentElement.getSuperclass();
            if (superclass != null && superclass.getKind() != TypeKind.NONE) {
                queue.add(superclass);
            }
            queue.addAll(currentElement.getInterfaces());
        }

        return false;
    }

    private String getGenericSupertypeFqn(TypeElement element, String targetSuperclassFqn) {
        Queue<TypeMirror> queue = new LinkedList<>();
        queue.add(element.asType());

        while (!queue.isEmpty()) {
            TypeMirror currentType = queue.poll();
            if (!(currentType instanceof DeclaredType)) {
                continue;
            }

            DeclaredType declaredType = (DeclaredType) currentType;
            TypeElement currentElement = (TypeElement) declaredType.asElement();
            if (currentElement.getQualifiedName().toString().equals(targetSuperclassFqn)) {
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                if (!typeArguments.isEmpty()) {
                    TypeMirror genericArg = typeArguments.get(0);
                    if (genericArg.getKind() == TypeKind.DECLARED) {
                        return ((TypeElement) typeUtils.asElement(genericArg)).getQualifiedName().toString();
                    }
                }
                return null;
            }
            TypeMirror superclass = currentElement.getSuperclass();
            if (superclass.getKind() != TypeKind.NONE) {
                queue.add(superclass);
            }
            queue.addAll(currentElement.getInterfaces());
        }

        return null;
    }

    private static class EntityInfo {
        final String fqn, name;
        final int trackingRange, updateFrequency;
        final boolean sendVelocityUpdates;
        final int[] eggColors;

        EntityInfo(String fqn, String name, int r, int u, boolean v, int[] eggColors) {
            this.fqn = fqn;
            this.name = name;
            this.trackingRange = r;
            this.updateFrequency = u;
            this.sendVelocityUpdates = v;
            this.eggColors = eggColors;
        }
    }

    private static class EntityRendererInfo {
        final String entityFqn, rendererFqn, factoryFieldName;

        EntityRendererInfo(String entityFqn, String rendererFqn, String factoryFieldName) {
            this.entityFqn = entityFqn;
            this.rendererFqn = rendererFqn;
            this.factoryFieldName = factoryFieldName;
        }
    }

    private static class TeisrInfo {
        final String rendererFqn;
        final String itemFieldName;
        final String constructorArgs;
        final String instanceFieldName;

        TeisrInfo(String rendererFqn, String itemFieldName, String constructorArgs, String instanceFieldName) {
            this.rendererFqn = rendererFqn;
            this.itemFieldName = itemFieldName;
            this.constructorArgs = constructorArgs;
            this.instanceFieldName = instanceFieldName;
        }
    }
}
