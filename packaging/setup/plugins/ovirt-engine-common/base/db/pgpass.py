#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""DB pgpass plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import database


@util.export
class Plugin(plugin.PluginBase):
    """DB pgpass plugin."""
    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment[osetupcons.DBEnv.PGPASS_FILE] = None

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        name=osetupcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        condition=lambda self: self.environment[
            osetupcons.DBEnv.PASSWORD
        ] is not None
    )
    def _validation(self):
        # this required for dbvalidations
        database.OvirtUtils(
            plugin=self,
            dbenvkeys=osetupcons.Const.ENGINE_DB_ENV_KEYS,
        ).createPgPass()

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=osetupcons.Stages.DB_CREDENTIALS_AVAILABLE_LATE,
    )
    def _misc(self):
        database.OvirtUtils(
            plugin=self,
            dbenvkeys=osetupcons.Const.ENGINE_DB_ENV_KEYS,
        ).createPgPass()


# vim: expandtab tabstop=4 shiftwidth=4
