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


""" DB validations plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """ DB validations plugin."""

    def _dbUtil(self, fix=False):

        args = [
            osetupcons.FileLocations.OVIRT_ENGINE_DB_VALIDATOR,
            '--user={user}'.format(
                user=self.environment[
                    osetupcons.DBEnv.USER
                ],
            ),
            '--host={host}'.format(
                host=self.environment[
                    osetupcons.DBEnv.HOST
                ],
            ),
            '--port={port}'.format(
                port=self.environment[
                    osetupcons.DBEnv.PORT
                ],
            ),
            '--database={database}'.format(
                database=self.environment[
                    osetupcons.DBEnv.DATABASE
                ],
            ),
            '--log={logfile}'.format(
                logfile=self.environment[
                    otopicons.CoreEnv.LOG_FILE_NAME
                ],
            ),
        ]
        if fix:
            args.append('--fix')

        return self.execute(
            args,
            raiseOnError=False,
            envAppend={
                'DBFUNC_DB_PGPASSFILE': self.environment[
                    osetupcons.DBEnv.PGPASS_FILE
                ]
            },
        )

    def _checkDb(self):

        rc, stdout, stderr = self._dbUtil()
        if rc != 0:
            raise RuntimeError(
                _(
                    'Failed checking Engine database:\n'
                    '{output}\n'.format(
                        output=stdout,
                    )
                )
            )

        return (stdout, rc)

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.DBEnv.FIX_DB_VIOLATIONS,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        priority=plugin.Stages.PRIORITY_LOW,
        after=(
            osetupcons.Stages.DB_CONNECTION_CUSTOMIZATION,
            osetupcons.Stages.DB_CREDENTIALS_AVAILABLE_EARLY,
        ),
        condition=lambda self: not self.environment[
            osetupcons.DBEnv.NEW_DATABASE
        ],
    )
    def _validation(self):
        self.logger.info(
            _('Checking the Engine database consistency')
        )
        violations, issues_found = self._checkDb()
        if issues_found:
            if self.environment[
                osetupcons.DBEnv.FIX_DB_VIOLATIONS
            ] is None:
                self.logger.warn(
                    _(
                        'The following inconsistencies were found '
                        'in Engine database: {violations}. '
                    ).format(
                        violations=violations,
                    ),
                )
                self.environment[
                    osetupcons.DBEnv.FIX_DB_VIOLATIONS
                ] = self.dialog.queryBoolean(
                    dialog=self.dialog,
                    name='OVESETUP_FIX_DB_VALIDATIONS',
                    note=_(
                        'Would you like to automatically clear '
                        'inconsistencies before upgraing?\n'
                        '(Answering no will stop the upgrade): '
                    ),
                    prompt=True,
                )

            if not self.environment[
                osetupcons.DBEnv.FIX_DB_VIOLATIONS
            ]:
                raise RuntimeError(
                    _(
                        'Upgrade aborted, database integrity '
                        'cannot be established.'
                    )
                )

    @plugin.event(
        stage=plugin.Stages.STAGE_EARLY_MISC,
        condition=lambda self: self.environment[
            osetupcons.DBEnv.FIX_DB_VIOLATIONS
        ],
    )
    def _misc(self):
        self.logger.info(
            _('Fixing Engine database inconsistencies')
        )
        self._dbUtil(fix=True)


# vim: expandtab tabstop=4 shiftwidth=4
