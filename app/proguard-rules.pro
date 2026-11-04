# Manual de Seguridad, sección 12.1 / TRACEABILITY.md
# Eliminar invocaciones a Timber debug en bytecode de producción
-assumenosideeffects class timber.log.Timber {
    public static void d(...);
    public static void v(...);
}
