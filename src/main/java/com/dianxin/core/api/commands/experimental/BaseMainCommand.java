package com.dianxin.core.api.commands.experimental;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class BaseMainCommand implements MaincommandRegistry {



    @Override
    public abstract CommandData getCommand();
}
