package com.sqless.sqlessmobile.ui.adapters;

import android.content.Context;

public interface Subtitulado {

    String getTitulo();

    String getSubtitulo();

    /**
     * Retorna el titulo que le corresponderá a esta entidad. De no querer implementar este método,
     * dejar que devuelva un null o String vacía.
     *
     * @param context El contexto en el cual aparecerá esta entidad. Este parámetro nos da acceso a
     *                los Resources de string, etc.
     * @return un string que irá como titulo.
     */
    String getTitulo(Context context);

    /**
     * Retorna el subtitulo que le corresponderá a esta entidad.
     *
     * @param context El contexto en el cual aparecerá esta entidad. Este parámetro nos da acceso a
     *                los Resources de string, etc.
     * @return un string que irá como subtitulo.
     */
    String getSubtitulo(Context context);
}
