package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateCloneOfTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

/**
 * This command responsible to creating a copy of template image. Usually it
 * will be called during Create Vm From Template.
 */
@InternalCommandAttribute
public class CreateCloneOfTemplateCommand<T extends CreateCloneOfTemplateParameters> extends
        CreateSnapshotFromTemplateCommand<T> {
    public CreateCloneOfTemplateCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected DiskImage cloneDiskImage(Guid newImageGuid) {
        DiskImage returnValue = super.cloneDiskImage(newImageGuid);
        returnValue.setStorageIds(new ArrayList<Guid>(Arrays.asList(getDestinationStorageDomainId())));
        returnValue.setQuotaId(getParameters().getQuotaId());
        // override to have no template
        returnValue.setParentId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        returnValue.setImageTemplateId(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        if (getParameters().getDiskImageBase() != null) {
            returnValue.setVolumeType(getParameters().getDiskImageBase().getVolumeType());
            returnValue.setvolumeFormat(getParameters().getDiskImageBase().getVolumeFormat());
        }
        return returnValue;
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        setDestinationImageId(Guid.newGuid());
        mNewCreatedDiskImage = cloneDiskImage(getDestinationImageId());
        mNewCreatedDiskImage.setId(Guid.newGuid());
        Guid storagePoolID = mNewCreatedDiskImage.getStoragePoolId() != null ? mNewCreatedDiskImage
                .getStoragePoolId() : Guid.Empty;

        VDSReturnValue vdsReturnValue = null;
        Guid taskId = persistAsyncTaskPlaceHolder(VdcActionType.AddVmFromTemplate);
        try {
            vdsReturnValue = runVdsCommand(VDSCommandType.CopyImage,
                    new CopyImageVDSCommandParameters(storagePoolID, getDiskImage().getStorageIds().get(0),
                            getVmTemplateId(), getDiskImage().getId(), getImage().getImageId(),
                            mNewCreatedDiskImage.getId(), getDestinationImageId(),
                            "", getDestinationStorageDomainId(), CopyVolumeType.LeafVol,
                            mNewCreatedDiskImage.getVolumeFormat(), mNewCreatedDiskImage.getVolumeType(),
                            getDiskImage().isWipeAfterDelete(), false));

        } catch (VdcBLLException e) {
            log.errorFormat("Failed creating snapshot from image id -'{0}'", getImage().getImageId());
            throw e;
        }

        if (vdsReturnValue.getSucceeded()) {
            getReturnValue().getInternalVdsmTaskIdList().add(
                    createTask(taskId,
                            vdsReturnValue.getCreationInfo(),
                            VdcActionType.AddVmFromTemplate,
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId(),
                            getDestinationStorageDomainId()));
        }

        return vdsReturnValue;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.copyImage;
    }
}
