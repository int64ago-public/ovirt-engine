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


"""
AIO plugin.
"""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin
from otopi import filetransaction
from otopi import constants as otopicons

from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import dialog


@util.export
class Plugin(plugin.PluginBase):
    """
    AIO plugin.
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.AIOEnv.ENABLE,
            False
        )
        self.environment.setdefault(
            osetupcons.AIOEnv.CONFIGURE,
            None
        )
        self.environment.setdefault(
            osetupcons.AIOEnv.CONTINUE_WITHOUT_AIO,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        after=(
            osetupcons.Stages.DB_CONNECTION_SETUP,
        ),
        condition=lambda self: self.environment[
            osetupcons.AIOEnv.ENABLE
        ] and self.environment[
            osetupcons.DBEnv.NEW_DATABASE
        ],
    )
    def _setup(self):
        self._enabled = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: (
            self._enabled and
            self.environment[
                osetupcons.AIOEnv.SUPPORTED
            ] is False
        ),
        name=osetupcons.Stages.AIO_CONFIG_NOT_AVAILABLE,
        before=(
            osetupcons.Stages.AIO_CONFIG_AVAILABLE,
        ),
    )
    def _continueSetupWithoutAIO(self):
        if self.environment[
            osetupcons.AIOEnv.CONTINUE_WITHOUT_AIO
        ] is None:
            self.environment[
                osetupcons.AIOEnv.CONTINUE_WITHOUT_AIO
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_CONTINUE_WITHOUT_AIO',
                note=_(
                    'Disabling all-in-one plugin because hardware '
                    'supporting virtualization could not be detected. '
                    'Do you want to continue setup without AIO plugin? '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=False,
            )

        if self.environment[
            osetupcons.AIOEnv.CONTINUE_WITHOUT_AIO
        ]:
            self._enabled = False
            self.environment[osetupcons.AIOEnv.CONFIGURE] = False
        else:
            raise RuntimeError(
                _('Aborted by user.')
            )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        condition=lambda self: self._enabled,
        name=osetupcons.Stages.AIO_CONFIG_AVAILABLE,
        before=(
            osetupcons.Stages.DIALOG_TITLES_E_ALLINONE,
        ),
        after=(
            osetupcons.Stages.DIALOG_TITLES_S_ALLINONE,
        ),
    )
    def _constomization(self):
        if self.environment[
            osetupcons.AIOEnv.CONFIGURE
        ] is None:
            self.environment[
                osetupcons.AIOEnv.CONFIGURE
            ] = dialog.queryBoolean(
                dialog=self.dialog,
                name='OVESETUP_AIO_CONFIGURE',
                note=_(
                    'Configure VDSM on this host? '
                    '(@VALUES@) [@DEFAULT@]: '
                ),
                prompt=True,
                default=False,
            )
        if self.environment[osetupcons.AIOEnv.CONFIGURE]:
            self.environment[
                osetupcons.ConfigEnv.FQDN_REVERSE_VALIDATION
            ] = True
            self.environment[
                osetupcons.ConfigEnv.FQDN_NON_LOOPBACK_VALIDATION
            ] = True

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        condition=lambda self: self.environment[
            osetupcons.AIOEnv.CONFIGURE
        ],
    )
    def _misc(self):
        """
        Disable aio in future setups.
        """
        self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
            filetransaction.FileTransaction(
                name=osetupcons.FileLocations.AIO_POST_INSTALL_CONFIG,
                content=(
                    '[environment:default]',
                    'OVESETUP_AIO/enable=bool:False',
                ),
                modifiedList=self.environment[
                    otopicons.CoreEnv.MODIFIED_FILES
                ],
            )
        )


# vim: expandtab tabstop=4 shiftwidth=4
