package com.hbm.main;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.hbm.config.MachineDynConfig;
import com.hbm.tileentity.IConfigurableMachine;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class AutoRegistry {
    public static final List<Class<? extends IConfigurableMachine>> configurableMachineClasses = new ArrayList<>();
    private static final String GENERATED_REGISTRAR_CLASS = "com.hbm.generated.GeneratedHBMRegistrar";

    @CanIgnoreReturnValue
    static int registerEntities(int startId) {
        try {
            Class<?> registrarClass = Class.forName(GENERATED_REGISTRAR_CLASS);
            Method method = registrarClass.getMethod("registerEntities", int.class);
            Object result = method.invoke(null, startId);
            int nextId = (Integer) result;
            MainRegistry.logger.debug("Entity registration complete. Next available ID is now: {}", nextId);
            return nextId;
        } catch (NoSuchMethodException e) {
            MainRegistry.logger.debug("Registration method 'registerEntities(int)' not found. Skipping entity registration.");
            return startId;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find the generated registrar class '" + GENERATED_REGISTRAR_CLASS + "'. Did the annotation " +
                    "processor run correctly?", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke 'registerEntities' from generated registrar.", e);
        }
    }

    static void registerTileEntities() {
        MainRegistry.logger.debug("Beginning automatic TileEntity registration...");
        invokeRegistrationMethod("registerTileEntities");
    }

    static void preInitClient() {
        invokeRegistrationMethod("registerItemRenderers");
    }

    @SideOnly(Side.CLIENT)
    static void registerRenderInfo() {
        invokeRegistrationMethod("registerEntityRenderers");
        invokeRegistrationMethod("registerTileEntityRenderers");
    }

    static void loadAuxiliaryData() {
        MainRegistry.logger.debug("Loading auxiliary registration data...");
        try {
            Class<?> registrarClass = Class.forName(GENERATED_REGISTRAR_CLASS);
            Field field = registrarClass.getField("CONFIGURABLE_MACHINES");
            //noinspection unchecked
            List<Class<? extends IConfigurableMachine>> foundClasses = (List<Class<? extends IConfigurableMachine>>) field.get(null);
            configurableMachineClasses.addAll(foundClasses);
            MachineDynConfig.initialize();
            MainRegistry.logger.debug("Successfully loaded {} configurable machine classes.", configurableMachineClasses.size());
        } catch (NoSuchFieldException e) {
            MainRegistry.logger.debug("Field 'CONFIGURABLE_MACHINES' not found. Skipping (this is normal if no machines are configurable).");
        } catch (ClassNotFoundException e) {
            MainRegistry.logger.debug("Could not find generated registrar class to load configurable machine data.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configurable machine data from generated registrar.", e);
        }
    }

    private static void invokeRegistrationMethod(String methodName) {
        try {
            Class<?> registrarClass = Class.forName(GENERATED_REGISTRAR_CLASS);
            Method method = registrarClass.getMethod(methodName);
            method.invoke(null);
            MainRegistry.logger.debug("Successfully invoked registration method: {}", methodName);
        } catch (NoSuchMethodException e) {
            MainRegistry.logger.debug("Registration method '{}' not found. Skipping (this is normal if no items of this type are registered).",
                    methodName);
        } catch (ClassNotFoundException e) {
            MainRegistry.logger.debug("Could not find generated registrar class to run '{}'.", methodName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke registration method '" + methodName + "' from generated registrar.", e);
        }
    }
}
