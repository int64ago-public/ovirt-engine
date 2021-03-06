package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.ByteSizeValidation;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NoTrimmingWhitespacesValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyQuotaValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotNullIntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.PoolNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class UnitVmModel extends Model {

    public static final int VM_TEMPLATE_NAME_MAX_LIMIT = 40;
    public static final int DESCRIPTION_MAX_LIMIT = 255;

    private boolean privateIsNew;

    private EntityModel<String> spiceProxy;
    public EntityModel<String> getSpiceProxy() {
        return spiceProxy;
    }

    public void setSpiceProxy(EntityModel<String> spiceProxy) {
        this.spiceProxy = spiceProxy;
    }

    private EntityModel<Boolean> spiceProxyEnabled;

    public EntityModel<Boolean> getSpiceProxyEnabled() {
        return spiceProxyEnabled;
    }

    public void setSpiceProxyEnabled(EntityModel<Boolean> spiceProxyEnabled) {
        this.spiceProxyEnabled = spiceProxyEnabled;
    }

    private EntityModel<Boolean> editingEnabled;

    public EntityModel<Boolean> getEditingEnabled() {
        return editingEnabled;
    }

    public void setEditingEnabled(EntityModel<Boolean> editingEnabled) {
        this.editingEnabled = editingEnabled;
    }

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    private boolean vmAttachedToPool;

    public boolean isVmAttachedToPool() {
        return vmAttachedToPool;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> isSoundcardEnabled;

    public EntityModel<Boolean> getIsSoundcardEnabled() {
        return isSoundcardEnabled;
    }

    private void setIsSoundcardEnabled(NotChangableForVmInPoolEntityModel<Boolean> isSoundcardEnabled) {
        this.isSoundcardEnabled = isSoundcardEnabled;
    }

    private NotChangableForVmInPoolListModel<DataCenterWithCluster> dataCenterWithClustersList;

    public ListModel<DataCenterWithCluster> getDataCenterWithClustersList() {
        return dataCenterWithClustersList;
    }

    private void setDataCenterWithClustersList(NotChangableForVmInPoolListModel<DataCenterWithCluster> dataCenterWithClustersList) {
        this.dataCenterWithClustersList = dataCenterWithClustersList;
    }

    private VnicInstancesModel nicsWithLogicalNetworks;

    public VnicInstancesModel getNicsWithLogicalNetworks() {
        return nicsWithLogicalNetworks;
    }

    public void setNicsWithLogicalNetworks(VnicInstancesModel nicsWithLogicalNetworks) {
        this.nicsWithLogicalNetworks = nicsWithLogicalNetworks;
    }

    /**
     * Note: We assume that this method is called only once, on the creation stage of the model. if this assumption is
     * changed (i.e the VM can attached/detached from a pool after the model is created), this method should be modified
     */
    public void setVmAttachedToPool(boolean value) {
        if (value) {
            // ==General Tab==
            getDataCenterWithClustersList().setIsChangable(!value);
            getQuota().setIsChangable(false);
            getDescription().setIsChangable(false);
            getComment().setIsChangable(false);

            getNumOfDesktops().setIsChangable(false);
            getPrestartedVms().setIsChangable(false);
            getMaxAssignedVmsPerUser().setIsChangable(false);

            getBaseTemplate().setIsChangable(false);
            getTemplate().setIsChangable(false);
            getMemSize().setIsChangable(false);
            getTotalCPUCores().setIsChangable(false);

            getCoresPerSocket().setIsChangable(false);
            getNumOfSockets().setIsChangable(false);
            getSerialNumberPolicy().setIsChangable(false);

            getOSType().setIsChangable(false);
            getIsStateless().setIsChangable(false);
            getIsRunAndPause().setIsChangable(false);
            getIsDeleteProtected().setIsChangable(false);

            // ==Initial run Tab==
            getTimeZone().setIsChangable(false);
            getDomain().setIsChangable(false);

            // ==Console Tab==
            getDisplayProtocol().setIsChangable(false);
            getUsbPolicy().setIsChangable(false);
            getNumOfMonitors().setIsChangable(false);
            getIsSingleQxlEnabled().setIsChangable(false);
            getIsSmartcardEnabled().setIsChangable(false);
            getAllowConsoleReconnect().setIsChangable(false);
            getVncKeyboardLayout().setIsChangable(false);
            getSsoMethodNone().setIsChangable(false);
            getSsoMethodGuestAgent().setIsChangable(false);

            // ==Host Tab==
            getIsAutoAssign().setIsChangable(false);
            getDefaultHost().setIsChangable(false);
            getHostCpu().setIsChangable(false);
            getMigrationMode().setIsChangable(false);
            getCpuPinning().setIsChangable(false);

            // ==Resource Allocation Tab==
            getMinAllocatedMemory().setIsChangable(false);
            getProvisioning().setIsChangable(false);
            getProvisioningThin_IsSelected().setIsChangable(false);
            getProvisioningClone_IsSelected().setIsChangable(false);
            getDisksAllocationModel().setIsChangable(false);

            // ==Boot Options Tab==
            getFirstBootDevice().setIsChangable(false);
            getSecondBootDevice().setIsChangable(false);
            getCdAttached().setIsChangable(false);
            getCdImage().setIsChangable(false);
            getKernel_path().setIsChangable(false);
            getInitrd_path().setIsChangable(false);
            getKernel_parameters().setIsChangable(false);

            // ==Custom Properties Tab==
            getCustomProperties().setIsChangable(false);

            vmAttachedToPool = true;
        }
    }

    private String privateHash;

    public String getHash()
    {
        return privateHash;
    }

    public void setHash(String value)
    {
        privateHash = value;
    }

    private boolean isBlankTemplate;

    public boolean getIsBlankTemplate()
    {
        return isBlankTemplate;
    }

    public void setIsBlankTemplate(boolean value)
    {
        if (isBlankTemplate != value)
        {
            isBlankTemplate = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsBlankTemplate")); //$NON-NLS-1$
        }
    }

    private boolean isWindowsOS;

    public boolean getIsWindowsOS()
    {
        return isWindowsOS;
    }

    public void setIsWindowsOS(boolean value)
    {
        if (isWindowsOS != value)
        {
            isWindowsOS = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsWindowsOS")); //$NON-NLS-1$
        }
    }

    private boolean isLinuxOS;

    public boolean getIsLinuxOS()
    {
        return isLinuxOS;
    }

    public void setIsLinuxOS(boolean value)
    {
        if (isLinuxOS != value)
        {
            isLinuxOS = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsLinuxOS")); //$NON-NLS-1$
        }
    }

    private String cpuNotification;

    public String getCPUNotification()
    {
        return cpuNotification;
    }

    public void setCPUNotification(String value)
    {
        if (!ObjectUtils.objectsEqual(cpuNotification, value))
        {
            cpuNotification = value;
            onPropertyChanged(new PropertyChangedEventArgs("CPUNotification")); //$NON-NLS-1$
        }
    }

    public boolean isCPUsAmountValid;

    public boolean getIsCPUsAmountValid()
    {
        return isCPUsAmountValid;
    }

    public void setIsCPUsAmountValid(boolean value)
    {
        if (isCPUsAmountValid != value)
        {
            isCPUsAmountValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCPUsAmountValid")); //$NON-NLS-1$
        }
    }

    private boolean isGeneralTabValid;

    public boolean getIsGeneralTabValid()
    {
        return isGeneralTabValid;
    }

    public void setIsGeneralTabValid(boolean value)
    {
        if (isGeneralTabValid != value)
        {
            isGeneralTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid")); //$NON-NLS-1$
        }
    }

    private boolean isFirstRunTabValid;

    public boolean getIsFirstRunTabValid()
    {
        return isFirstRunTabValid;
    }

    public void setIsFirstRunTabValid(boolean value)
    {
        if (isFirstRunTabValid != value)
        {
            isFirstRunTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsFirstRunTabValid")); //$NON-NLS-1$
        }
    }

    private boolean isDisplayTabValid;

    public boolean getIsDisplayTabValid()
    {
        return isDisplayTabValid;
    }

    public void setIsDisplayTabValid(boolean value)
    {
        if (isDisplayTabValid != value)
        {
            isDisplayTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsDisplayTabValid")); //$NON-NLS-1$
        }
    }

    private boolean isAllocationTabValid;

    public boolean getIsAllocationTabValid()
    {
        return isAllocationTabValid;
    }

    public void setIsAllocationTabValid(boolean value)
    {
        if (isAllocationTabValid != value)
        {
            isAllocationTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsAllocationTabValid")); //$NON-NLS-1$
        }
    }

    private boolean isHostTabValid;

    public boolean getIsHostTabValid()
    {
        return isHostTabValid;
    }

    public void setIsHostTabValid(boolean value)
    {
        if (isHostTabValid != value)
        {
            isHostTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsHostTabValid")); //$NON-NLS-1$
        }
    }

    private boolean isBootSequenceTabValid;

    public boolean getIsBootSequenceTabValid()
    {
        return isBootSequenceTabValid;
    }

    public void setIsBootSequenceTabValid(boolean value)
    {
        if (isBootSequenceTabValid != value)
        {
            isBootSequenceTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsBootSequenceTabValid")); //$NON-NLS-1$
        }
    }

    private boolean isCustomPropertiesTabValid;

    public boolean getIsCustomPropertiesTabValid()
    {
        return isCustomPropertiesTabValid;
    }

    public void setIsCustomPropertiesTabValid(boolean value)
    {
        if (isCustomPropertiesTabValid != value)
        {
            isCustomPropertiesTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCustomPropertiesTabValid")); //$NON-NLS-1$
        }
    }

    private NotChangableForVmInPoolListModel<StorageDomain> privateStorageDomain;

    public ListModel<StorageDomain> getStorageDomain()
    {
        return privateStorageDomain;
    }

    private void setStorageDomain(NotChangableForVmInPoolListModel<StorageDomain> value)
    {
        privateStorageDomain = value;
    }

    private NotChangableForVmInPoolListModel<VmTemplate> privateTemplate;

    public ListModel<VmTemplate> getTemplate()
    {
        return privateTemplate;
    }

    private void setTemplate(NotChangableForVmInPoolListModel<VmTemplate> value)
    {
        privateTemplate = value;
    }

    private NotChangableForVmInPoolListModel<VmTemplate> baseTemplate;

    public ListModel<VmTemplate> getBaseTemplate()
    {
        return baseTemplate;
    }

    private void setBaseTemplate(NotChangableForVmInPoolListModel<VmTemplate> value)
    {
        baseTemplate = value;
    }

    private NotChangableForVmInPoolListModel<VmType> vmType;

    public void setVmType(NotChangableForVmInPoolListModel<VmType> vmType) {
        this.vmType = vmType;
    }

    public ListModel<VmType> getVmType() {
        return vmType;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName()
    {
        return privateName;
    }

    private void setName(EntityModel<String> value)
    {
        privateName = value;
    }

    private NotChangableForVmInPoolListModel<Integer> privateOSType;

    public ListModel<Integer> getOSType()
    {
        return privateOSType;
    }

    private void setOSType(NotChangableForVmInPoolListModel<Integer> value)
    {
        privateOSType = value;
    }

    private NotChangableForVmInPoolListModel<Integer> privateNumOfMonitors;

    public ListModel<Integer> getNumOfMonitors()
    {
        return privateNumOfMonitors;
    }

    private void setNumOfMonitors(NotChangableForVmInPoolListModel<Integer> value)
    {
        privateNumOfMonitors = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsSingleQxlEnabled;

    public EntityModel<Boolean> getIsSingleQxlEnabled()
    {
        return privateIsSingleQxlEnabled;
    }

    private void setIsSingleQxlEnabled(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateIsSingleQxlEnabled = value;
    }


    private NotChangableForVmInPoolEntityModel<Boolean> privateAllowConsoleReconnect;

    public EntityModel<Boolean> getAllowConsoleReconnect()
    {
        return privateAllowConsoleReconnect;
    }

    private void setAllowConsoleReconnect(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateAllowConsoleReconnect = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateDescription;

    public EntityModel<String> getDescription()
    {
        return privateDescription;
    }

    private void setDescription(NotChangableForVmInPoolEntityModel<String> value)
    {
        privateDescription = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateComment;

    public EntityModel<String> getComment()
    {
        return privateComment;
    }

    private void setComment(NotChangableForVmInPoolEntityModel<String> value)
    {
        privateComment = value;
    }

    private NotChangableForVmInPoolEntityModel<String> templateVersionName;

    public EntityModel<String> getTemplateVersionName()
    {
        return templateVersionName;
    }

    private void setTemplateVersionName(NotChangableForVmInPoolEntityModel<String> value)
    {
        templateVersionName = value;
    }

    private NotChangableForVmInPoolListModel<String> privateDomain;

    public ListModel<String> getDomain()
    {
        return privateDomain;
    }

    private void setDomain(NotChangableForVmInPoolListModel<String> value)
    {
        privateDomain = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> privateMemSize;

    public EntityModel<Integer> getMemSize()
    {
        return privateMemSize;
    }

    private void setMemSize(NotChangableForVmInPoolEntityModel<Integer> value)
    {
        privateMemSize = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> privateMinAllocatedMemory;

    public EntityModel<Integer> getMinAllocatedMemory()
    {
        return privateMinAllocatedMemory;
    }

    private void setMinAllocatedMemory(NotChangableForVmInPoolEntityModel<Integer> value)
    {
        privateMinAllocatedMemory = value;
    }

    private NotChangableForVmInPoolListModel<Quota> privateQuota;

    public ListModel<Quota> getQuota()
    {
        return privateQuota;
    }

    private void setQuota(NotChangableForVmInPoolListModel<Quota> value)
    {
        privateQuota = value;
    }

    private NotChangableForVmInPoolListModel<UsbPolicy> privateUsbPolicy;

    public ListModel<UsbPolicy> getUsbPolicy()
    {
        return privateUsbPolicy;
    }

    private void setUsbPolicy(NotChangableForVmInPoolListModel<UsbPolicy> value)
    {
        privateUsbPolicy = value;
    }

    private NotChangableForVmInPoolListModel<TimeZoneModel> privateTimeZone;

    public ListModel<TimeZoneModel> getTimeZone()
    {
        return privateTimeZone;
    }

    private void setTimeZone(NotChangableForVmInPoolListModel<TimeZoneModel> value)
    {
        privateTimeZone = value;
    }

    private NotChangableForVmInPoolListModel<Integer> privateNumOfSockets;

    public ListModel<Integer> getNumOfSockets()
    {
        return privateNumOfSockets;
    }

    private void setNumOfSockets(NotChangableForVmInPoolListModel<Integer> value)
    {
        privateNumOfSockets = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateTotalCPUCores;

    public EntityModel<String> getTotalCPUCores()
    {
        return privateTotalCPUCores;
    }

    private void setTotalCPUCores(NotChangableForVmInPoolEntityModel<String> value)
    {
        privateTotalCPUCores = value;
    }

    private NotChangableForVmInPoolListModel<Integer> privateCoresPerSocket;

    public ListModel<Integer> getCoresPerSocket()
    {
        return privateCoresPerSocket;
    }

    private void setCoresPerSocket(NotChangableForVmInPoolListModel<Integer> value)
    {
        privateCoresPerSocket = value;
    }

    private NotChangableForVmInPoolListModel<VDS> privateDefaultHost;

    public ListModel<VDS> getDefaultHost()
    {
        return privateDefaultHost;
    }

    private void setDefaultHost(NotChangableForVmInPoolListModel<VDS> value)
    {
        privateDefaultHost = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateisSmartcardEnabled;

    public EntityModel<Boolean> getIsSmartcardEnabled()
    {
        return privateisSmartcardEnabled;
    }

    private void setIsSmartcardEnabled(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateisSmartcardEnabled = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> isConsoleDeviceEnabled;

    public EntityModel<Boolean> getIsConsoleDeviceEnabled() {
        return isConsoleDeviceEnabled;
    }

    private void setConsoleDeviceEnabled(NotChangableForVmInPoolEntityModel<Boolean> consoleDeviceEnabled) {
        this.isConsoleDeviceEnabled = consoleDeviceEnabled;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsStateless;

    public EntityModel<Boolean> getIsStateless()
    {
        return privateIsStateless;
    }

    private void setIsStateless(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateIsStateless = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsRunAndPause;

    public EntityModel<Boolean> getIsRunAndPause()
    {
        return privateIsRunAndPause;
    }

    private void setIsRunAndPause(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateIsRunAndPause = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsDeleteProtected;

    public EntityModel<Boolean> getIsDeleteProtected() {
        return privateIsDeleteProtected;
    }

    public void setIsDeleteProtected(NotChangableForVmInPoolEntityModel<Boolean> deleteProtected) {
        this.privateIsDeleteProtected = deleteProtected;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> ssoMethodNone;

    public EntityModel<Boolean> getSsoMethodNone() {
        return ssoMethodNone;
    }

    public void setSsoMethodNone(NotChangableForVmInPoolEntityModel<Boolean> ssoMethodNone) {
        this.ssoMethodNone = ssoMethodNone;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> ssoMethodGuestAgent;

    public EntityModel<Boolean> getSsoMethodGuestAgent() {
        return ssoMethodGuestAgent;
    }

    public void setSsoMethodGuestAgent(NotChangableForVmInPoolEntityModel<Boolean> ssoMethodGuestAgent) {
        this.ssoMethodGuestAgent = ssoMethodGuestAgent;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> copyPermissions;

    public EntityModel<Boolean> getCopyPermissions() {
        return copyPermissions;
    }

    private void setCopyPermissions(NotChangableForVmInPoolEntityModel<Boolean> copyPermissions) {
        this.copyPermissions = copyPermissions;
    }

    private EntityModel<Boolean> memoryBalloonDeviceEnabled;

    public EntityModel<Boolean> getMemoryBalloonDeviceEnabled() {
        return memoryBalloonDeviceEnabled;
    }

    public void setMemoryBalloonDeviceEnabled(EntityModel<Boolean> memoryBalloonDeviceEnabled) {
        this.memoryBalloonDeviceEnabled = memoryBalloonDeviceEnabled;
    }

    private NotChangableForVmInPoolListModel<EntityModel<DisplayType>> privateDisplayProtocol;

    public ListModel<EntityModel<DisplayType>> getDisplayProtocol()
    {
        return privateDisplayProtocol;
    }

    private void setDisplayProtocol(NotChangableForVmInPoolListModel<EntityModel<DisplayType>> value)
    {
        privateDisplayProtocol = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateProvisioning;

    public EntityModel<Boolean> getProvisioning()
    {
        return privateProvisioning;
    }

    private void setProvisioning(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateProvisioning = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateProvisioningThin_IsSelected;

    public EntityModel<Boolean> getProvisioningThin_IsSelected()
    {
        return privateProvisioningThin_IsSelected;
    }

    public void setProvisioningThin_IsSelected(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateProvisioningThin_IsSelected = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateProvisioningClone_IsSelected;

    public EntityModel<Boolean> getProvisioningClone_IsSelected()
    {
        return privateProvisioningClone_IsSelected;
    }

    public void setProvisioningClone_IsSelected(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateProvisioningClone_IsSelected = value;
    }

    private EntityModel<Boolean> isVirtioScsiEnabled;

    public EntityModel<Boolean> getIsVirtioScsiEnabled() {
        return isVirtioScsiEnabled;
    }

    public void setIsVirtioScsiEnabled(EntityModel<Boolean> virtioScsiEnabled) {
        this.isVirtioScsiEnabled = virtioScsiEnabled;
    }

    private NotChangableForVmInPoolListModel<EntityModel<Integer>> privatePriority;

    public ListModel<EntityModel<Integer>> getPriority()
    {
        return privatePriority;
    }

    private void setPriority(NotChangableForVmInPoolListModel<EntityModel<Integer>> value)
    {
        privatePriority = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsHighlyAvailable;

    public EntityModel<Boolean> getIsHighlyAvailable()
    {
        return privateIsHighlyAvailable;
    }

    private void setIsHighlyAvailable(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateIsHighlyAvailable = value;
    }

    private NotChangableForVmInPoolListModel<EntityModel<BootSequence>> privateFirstBootDevice;

    public ListModel<EntityModel<BootSequence>> getFirstBootDevice()
    {
        return privateFirstBootDevice;
    }

    private void setFirstBootDevice(NotChangableForVmInPoolListModel<EntityModel<BootSequence>> value)
    {
        privateFirstBootDevice = value;
    }

    private NotChangableForVmInPoolListModel<EntityModel<BootSequence>> privateSecondBootDevice;

    public ListModel<EntityModel<BootSequence>> getSecondBootDevice()
    {
        return privateSecondBootDevice;
    }

    private void setSecondBootDevice(NotChangableForVmInPoolListModel<EntityModel<BootSequence>> value)
    {
        privateSecondBootDevice = value;
    }

    private NotChangableForVmInPoolListModel<String> privateCdImage;

    public ListModel<String> getCdImage()
    {
        return privateCdImage;
    }

    private void setCdImage(NotChangableForVmInPoolListModel<String> value)
    {
        privateCdImage = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> isSubTemplate;

    public EntityModel<Boolean> getIsSubTemplate() {
        return isSubTemplate;
    }

    public void setIsSubTemplate(NotChangableForVmInPoolEntityModel<Boolean> value) {
        isSubTemplate = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> cdAttached;

    public EntityModel<Boolean> getCdAttached() {
        return cdAttached;
    }

    public void setCdAttached(NotChangableForVmInPoolEntityModel<Boolean> value) {
        cdAttached = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateInitrd_path;

    public EntityModel<String> getInitrd_path()
    {
        return privateInitrd_path;
    }

    private void setInitrd_path(NotChangableForVmInPoolEntityModel<String> value)
    {
        privateInitrd_path = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateKernel_path;

    public EntityModel<String> getKernel_path()
    {
        return privateKernel_path;
    }

    private void setKernel_path(NotChangableForVmInPoolEntityModel<String> value)
    {
        privateKernel_path = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateKernel_parameters;

    public EntityModel<String> getKernel_parameters()
    {
        return privateKernel_parameters;
    }

    private void setKernel_parameters(NotChangableForVmInPoolEntityModel<String> value)
    {
        privateKernel_parameters = value;
    }

    private NotChangableForVmInPoolEntityModel<String> privateCustomProperties;

    public EntityModel<String> getCustomProperties()
    {
        return privateCustomProperties;
    }

    private void setCustomProperties(NotChangableForVmInPoolEntityModel<String> value)
    {
        privateCustomProperties = value;
    }

    private EntityModel<Boolean> vmInitEnabled;

    public EntityModel<Boolean> getVmInitEnabled() {
        return vmInitEnabled;
    }

    public void setVmInitEnabled(EntityModel<Boolean> vmInitEnabled) {
        this.vmInitEnabled = vmInitEnabled;
    }

    private EntityModel<Boolean> cloudInitEnabled;

    private EntityModel<Boolean> sysprepEnabled;

    public EntityModel<Boolean> getCloudInitEnabled() {
        return cloudInitEnabled;
    }

    public void setCloudInitEnabled(EntityModel<Boolean> cloudInitEnabled) {
        this.cloudInitEnabled = cloudInitEnabled;
    }

    public EntityModel<Boolean> getSysprepEnabled() {
        return sysprepEnabled;
    }

    public void setSysprepEnabled(EntityModel<Boolean> sysprepEnabled) {
        this.sysprepEnabled = sysprepEnabled;
    }

    private VmInitModel vmInitModel;

    public VmInitModel getVmInitModel() {
        return vmInitModel;
    }

    public void setVmInitModel(VmInitModel vmInitModel) {
        this.vmInitModel = vmInitModel;
    }


    private NotChangableForVmInPoolKeyValueModel customPropertySheet;

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(NotChangableForVmInPoolKeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
    }

    private HashMap<Version, ArrayList<String>> privateCustomPropertiesKeysList;

    public HashMap<Version, ArrayList<String>> getCustomPropertiesKeysList()
    {
        return privateCustomPropertiesKeysList;
    }

    public void setCustomPropertiesKeysList(HashMap<Version, ArrayList<String>> value)
    {
        privateCustomPropertiesKeysList = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsAutoAssign;

    public EntityModel<Boolean> getIsAutoAssign()
    {
        return privateIsAutoAssign;
    }

    public void setIsAutoAssign(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateIsAutoAssign = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> hostCpu;

    public EntityModel<Boolean> getHostCpu() {
        return hostCpu;
    }

    public void setHostCpu(NotChangableForVmInPoolEntityModel<Boolean> hostCpu) {
        this.hostCpu = hostCpu;
    }

    private NotChangableForVmInPoolListModel<MigrationSupport> migrationMode;

    public ListModel<MigrationSupport> getMigrationMode()
    {
        return migrationMode;
    }

    public void setMigrationMode(NotChangableForVmInPoolListModel<MigrationSupport> value)
    {
        migrationMode = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> overrideMigrationDowntime;

    public EntityModel<Boolean> getOverrideMigrationDowntime() {
        return overrideMigrationDowntime;
    }

    private void setOverrideMigrationDowntime(NotChangableForVmInPoolEntityModel<Boolean> value) {
        overrideMigrationDowntime = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> migrationDowntime;

    public EntityModel<Integer> getMigrationDowntime() {
        return migrationDowntime;
    }

    private void setMigrationDowntime(NotChangableForVmInPoolEntityModel<Integer> value) {
        migrationDowntime = value;
    }

    private NotChangableForVmInPoolEntityModel<Boolean> privateIsTemplatePublic;

    public EntityModel<Boolean> getIsTemplatePublic()
    {
        return privateIsTemplatePublic;
    }

    private void setIsTemplatePublic(NotChangableForVmInPoolEntityModel<Boolean> value)
    {
        privateIsTemplatePublic = value;
    }

    private boolean privateIsFirstRun;

    public boolean getIsFirstRun()
    {
        return privateIsFirstRun;
    }

    public void setIsFirstRun(boolean value)
    {
        privateIsFirstRun = value;
    }

    private List<DiskModel> disks;

    public List<DiskModel> getDisks()
    {
        return disks;
    }

    public void setDisks(List<DiskModel> value)
    {
        if (disks != value)
        {
            disks = value;
            onPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
        }
    }

    private DisksAllocationModel disksAllocationModel;

    public DisksAllocationModel getDisksAllocationModel()
    {
        return disksAllocationModel;
    }

    private void setDisksAllocationModel(DisksAllocationModel value)
    {
        disksAllocationModel = value;
    }

    private boolean isDisksAvailable;

    public boolean getIsDisksAvailable()
    {
        return isDisksAvailable;
    }

    public void setIsDisksAvailable(boolean value)
    {
        isDisksAvailable = value;
        onPropertyChanged(new PropertyChangedEventArgs("IsDisksAvailable")); //$NON-NLS-1$
    }

    private boolean isCustomPropertiesTabAvailable;

    public boolean getIsCustomPropertiesTabAvailable()
    {
        return isCustomPropertiesTabAvailable;
    }

    public void setIsCustomPropertiesTabAvailable(boolean value)
    {
        if (isCustomPropertiesTabAvailable != value)
        {
            isCustomPropertiesTabAvailable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCustomPropertiesTabAvailable")); //$NON-NLS-1$
        }
    }

    private final VmModelBehaviorBase behavior;

    public VmModelBehaviorBase getBehavior() {
        return behavior;
    }

    private void setBehavior(VmModelBehaviorBase value) {
    }

    private int _minMemSize = 1;

    public int get_MinMemSize()
    {
        return _minMemSize;
    }

    public void set_MinMemSize(int value)
    {
        _minMemSize = value;
    }

    private int _maxMemSize32 = 20480;

    public int get_MaxMemSize32()
    {
        return _maxMemSize32;
    }

    public void set_MaxMemSize32(int value)
    {
        _maxMemSize32 = value;
    }

    private int _maxMemSize64 = 2097152;

    public int get_MaxMemSize64()
    {
        return _maxMemSize64;
    }

    public void set_MaxMemSize64(int value)
    {
        _maxMemSize64 = value;
    }

    private NotChangableForVmInPoolEntityModel<String> cpuPinning;

    public EntityModel<String> getCpuPinning() {
        return cpuPinning;
    }

    public void setCpuPinning(NotChangableForVmInPoolEntityModel<String> cpuPinning) {
        this.cpuPinning = cpuPinning;
    }

    private NotChangableForVmInPoolEntityModel<Integer> cpuSharesAmount;

    public EntityModel<Integer> getCpuSharesAmount() {
        return cpuSharesAmount;
    }

    public void setCpuSharesAmount(NotChangableForVmInPoolEntityModel<Integer> cpuSharesAmount) {
        this.cpuSharesAmount = cpuSharesAmount;
    }

    private NotChangableForVmInPoolListModel<CpuSharesAmount> cpuSharesAmountSelection;

    public ListModel<CpuSharesAmount> getCpuSharesAmountSelection() {
        return cpuSharesAmountSelection;
    }

    public void setCpuSharesAmountSelection(NotChangableForVmInPoolListModel<CpuSharesAmount> cpuSharesAmountSelection) {
        this.cpuSharesAmountSelection = cpuSharesAmountSelection;
    }

    private ListModel<String> vncKeyboardLayout;

    public ListModel<String> getVncKeyboardLayout() {
        return vncKeyboardLayout;
    }

    public void setVncKeyboardLayout(ListModel<String> vncKeyboardLayout) {
        this.vncKeyboardLayout = vncKeyboardLayout;
    }

    private SerialNumberPolicyModel serialNumberPolicy;

    public SerialNumberPolicyModel getSerialNumberPolicy() {
        return serialNumberPolicy;
    }

    public void setSerialNumberPolicy(SerialNumberPolicyModel value) {
        this.serialNumberPolicy = value;
    }

    public UnitVmModel(VmModelBehaviorBase behavior) {
        Frontend.getInstance().getQueryStartedEvent().addListener(this);
        Frontend.getInstance().getQueryCompleteEvent().addListener(this);

        Frontend.getInstance().subscribe(new VdcQueryType[] { VdcQueryType.GetStorageDomainsByStoragePoolId,
                VdcQueryType.GetImagesListByStoragePoolId,
                VdcQueryType.GetDefaultTimeZone, VdcQueryType.GetStoragePoolsByClusterService,
                VdcQueryType.GetDomainList, VdcQueryType.GetConfigurationValue,
                VdcQueryType.GetVdsGroupsByStoragePoolId, VdcQueryType.GetVmTemplatesByStoragePoolId,
                VdcQueryType.GetVmTemplatesDisks, VdcQueryType.GetStorageDomainsByVmTemplateId,
                VdcQueryType.GetStorageDomainById, VdcQueryType.GetDataCentersWithPermittedActionOnClusters,
                VdcQueryType.GetClustersWithPermittedAction, VdcQueryType.GetVmTemplatesWithPermittedAction,
                VdcQueryType.GetVdsGroupById, VdcQueryType.GetStoragePoolById, VdcQueryType.GetAllDisksByVmId,
                VdcQueryType.GetVmTemplate, VdcQueryType.GetVmConfigurationBySnapshot, VdcQueryType.GetAllVdsGroups,
                VdcQueryType.GetPermittedStorageDomainsByStoragePoolId, VdcQueryType.GetHostsByClusterId,
                VdcQueryType.OsRepository,
                VdcQueryType.Search });

        this.behavior = behavior;
        this.behavior.setModel(this);

        setNicsWithLogicalNetworks(new VnicInstancesModel());
        setAdvancedMode(new EntityModel<Boolean>(false));
        setStorageDomain(new NotChangableForVmInPoolListModel<StorageDomain>());
        setName(new NotChangableForVmInPoolEntityModel<String>());
        setNumOfMonitors(new NotChangableForVmInPoolListModel<Integer>());
        setAllowConsoleReconnect(new NotChangableForVmInPoolEntityModel<Boolean>());
        setDescription(new NotChangableForVmInPoolEntityModel<String>());
        setComment(new NotChangableForVmInPoolEntityModel<String>());
        setDomain(new NotChangableForVmInPoolListModel<String>());
        setMinAllocatedMemory(new NotChangableForVmInPoolEntityModel<Integer>());
        setUsbPolicy(new NotChangableForVmInPoolListModel<UsbPolicy>());
        setIsStateless(new NotChangableForVmInPoolEntityModel<Boolean>());
        setIsRunAndPause(new NotChangableForVmInPoolEntityModel<Boolean>());
        setIsSmartcardEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        setIsDeleteProtected(new NotChangableForVmInPoolEntityModel<Boolean>());
        setSsoMethodNone(new NotChangableForVmInPoolEntityModel<Boolean>());
        setSsoMethodGuestAgent(new NotChangableForVmInPoolEntityModel<Boolean>());
        setConsoleDeviceEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        setCopyPermissions(new NotChangableForVmInPoolEntityModel<Boolean>());
        // by default not available - only for new VM
        getCopyPermissions().setIsAvailable(false);
        getCopyPermissions().setEntity(false);
        setVncKeyboardLayout(new NotChangableForVmInPoolListModel<String>());
        setVmType(new NotChangableForVmInPoolListModel<VmType>());
        getVmType().setItems(Arrays.asList(VmType.Desktop, VmType.Server));
        getVmType().setSelectedItem(VmType.Server);
        getVmType().setIsChangable(false);
        getVmType().getSelectedItemChangedEvent().addListener(this);

        setCdImage(new NotChangableForVmInPoolListModel<String>());
        getCdImage().setIsChangable(false);

        setMemoryBalloonDeviceEnabled(new EntityModel<Boolean>());
        getMemoryBalloonDeviceEnabled().setEntity(true);
        getMemoryBalloonDeviceEnabled().setIsAvailable(false);

        setSpiceProxyEnabled(new EntityModel<Boolean>());
        setSpiceProxy(new EntityModel<String>());

        setIsSubTemplate(new NotChangableForVmInPoolEntityModel<Boolean>(false));
        setTemplateVersionName(new NotChangableForVmInPoolEntityModel<String>());
        setBaseTemplate(new NotChangableForVmInPoolListModel<VmTemplate>());
        getBaseTemplate().getSelectedItemChangedEvent().addListener(this);

        setCdAttached(new NotChangableForVmInPoolEntityModel<Boolean>());
        getCdAttached().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                getCdImage().setIsChangable(getCdAttached().getEntity());
            }
        });
        getCdAttached().setEntity(false);

        setIsHighlyAvailable(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsHighlyAvailable().getEntityChangedEvent().addListener(this);
        setIsTemplatePublic(new NotChangableForVmInPoolEntityModel<Boolean>());
        setKernel_parameters(new NotChangableForVmInPoolEntityModel<String>());
        setKernel_path(new NotChangableForVmInPoolEntityModel<String>());
        setInitrd_path(new NotChangableForVmInPoolEntityModel<String>());
        setCustomProperties(new NotChangableForVmInPoolEntityModel<String>());
        setCustomPropertySheet(new NotChangableForVmInPoolKeyValueModel());
        setDisplayProtocol(new NotChangableForVmInPoolListModel<EntityModel<DisplayType>>());
        setSecondBootDevice(new NotChangableForVmInPoolListModel<EntityModel<BootSequence>>());
        setPriority(new NotChangableForVmInPoolListModel<EntityModel<Integer>>());
        setVmInitEnabled(new EntityModel(false));
        setCloudInitEnabled(new EntityModel());
        setSysprepEnabled(new EntityModel<Boolean>());
        getVmInitEnabled().getEntityChangedEvent().addListener(this);
        setVmInitModel(new VmInitModel());

        setTemplate(new NotChangableForVmInPoolListModel<VmTemplate>());
        getTemplate().getSelectedItemChangedEvent().addListener(this);

        setQuota(new NotChangableForVmInPoolListModel<Quota>());
        getQuota().setIsAvailable(false);

        setDataCenterWithClustersList(new NotChangableForVmInPoolListModel<DataCenterWithCluster>());
        getDataCenterWithClustersList().getSelectedItemChangedEvent().addListener(this);

        setTimeZone(new NotChangableForVmInPoolListModel<TimeZoneModel>());
        getTimeZone().getSelectedItemChangedEvent().addListener(this);

        setDefaultHost(new NotChangableForVmInPoolListModel<VDS>());
        getDefaultHost().getSelectedItemChangedEvent().addListener(this);

        setOSType(new NotChangableForVmInPoolListModel<Integer>() {
            @Override
            public void setSelectedItem(Integer value) {
                if (!AsyncDataProvider.osNameExists(value)) {
                    DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
                    VDSGroup cluster = dataCenterWithCluster == null ? null : dataCenterWithCluster.getCluster();
                    if (cluster == null) {
                        return;
                    }
                    super.setSelectedItem(AsyncDataProvider.getDefaultOs(cluster.getArchitecture()));
                } else {
                    super.setSelectedItem(value);
                }
            }
        });

        getOSType().getSelectedItemChangedEvent().addListener(this);

        setFirstBootDevice(new NotChangableForVmInPoolListModel<EntityModel<BootSequence>>());
        getFirstBootDevice().getSelectedItemChangedEvent().addListener(this);

        setProvisioning(new NotChangableForVmInPoolEntityModel<Boolean>());
        getProvisioning().getEntityChangedEvent().addListener(this);

        setMemSize(new NotChangableForVmInPoolEntityModel<Integer>());
        getMemSize().getEntityChangedEvent().addListener(this);

        setTotalCPUCores(new NotChangableForVmInPoolEntityModel<String>());
        getTotalCPUCores().getEntityChangedEvent().addListener(this);

        setNumOfSockets(new NotChangableForVmInPoolListModel<Integer>());
        getNumOfSockets().getSelectedItemChangedEvent().addListener(this);

        setCoresPerSocket(new NotChangableForVmInPoolListModel<Integer>());
        getCoresPerSocket().getSelectedItemChangedEvent().addListener(this);

        setSerialNumberPolicy(new SerialNumberPolicyModel());

        setMigrationMode(new NotChangableForVmInPoolListModel<MigrationSupport>());
        getMigrationMode().getSelectedItemChangedEvent().addListener(this);

        setOverrideMigrationDowntime(new NotChangableForVmInPoolEntityModel<Boolean>());
        getOverrideMigrationDowntime().getEntityChangedEvent().addListener(this);

        setMigrationDowntime(new NotChangableForVmInPoolEntityModel<Integer>());
        getMigrationDowntime().getEntityChangedEvent().addListener(this);

        setHostCpu(new NotChangableForVmInPoolEntityModel<Boolean>());
        getHostCpu().getEntityChangedEvent().addListener(this);

        setWatchdogAction(new NotChangableForVmInPoolListModel<String>());
        getWatchdogAction().getEntityChangedEvent().addListener(this);
        ArrayList<String> watchDogActions = new ArrayList<String>();
        for (VmWatchdogAction action : VmWatchdogAction.values()) {
            watchDogActions.add(EnumTranslator.createAndTranslate(action));
        }
        getWatchdogAction().setItems(watchDogActions);

        setWatchdogModel(new NotChangableForVmInPoolListModel<String>());
        getWatchdogModel().getEntityChangedEvent().addListener(this);

        setIsAutoAssign(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsAutoAssign().getEntityChangedEvent().addListener(this);

        setIsTemplatePublic(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsTemplatePublic().getEntityChangedEvent().addListener(this);

        setIsHostTabValid(true);
        setIsCustomPropertiesTabAvailable(true);
        setIsCustomPropertiesTabValid(getIsHostTabValid());
        setIsBootSequenceTabValid(getIsCustomPropertiesTabValid());
        setIsAllocationTabValid(getIsBootSequenceTabValid());
        setIsDisplayTabValid(getIsAllocationTabValid());
        setIsFirstRunTabValid(getIsDisplayTabValid());
        setIsGeneralTabValid(getIsFirstRunTabValid());

        // NOTE: This is because currently the auto generated view code tries to register events of
        // pooltype for
        // VM/Template views as this model is shared across VM/Template/Pool models
        setPoolType(new NotChangableForVmInPoolListModel<EntityModel<VmPoolType>>());

        setNumOfDesktops(new NotChangableForVmInPoolEntityModel<Integer>());
        getNumOfDesktops().setEntity(0);
        getNumOfDesktops().setIsAvailable(false);

        setAssignedVms(new NotChangableForVmInPoolEntityModel<Integer>());
        getAssignedVms().setEntity(0);
        getAssignedVms().setIsAvailable(false);
        // Assigned VMs count is always read-only.
        getAssignedVms().setIsChangable(false);

        setPrestartedVms(new NotChangableForVmInPoolEntityModel<Integer>());
        getPrestartedVms().setEntity(0);
        getPrestartedVms().setIsAvailable(false);

        setMaxAssignedVmsPerUser(new NotChangableForVmInPoolEntityModel<Integer>());
        getMaxAssignedVmsPerUser().setEntity(1);
        getMaxAssignedVmsPerUser().setIsAvailable(false);

        setDisksAllocationModel(new DisksAllocationModel());

        setIsVirtioScsiEnabled(new EntityModel<Boolean>());
        getIsVirtioScsiEnabled().setEntity(false);
        getIsVirtioScsiEnabled().setIsAvailable(false);

        setProvisioningClone_IsSelected(new NotChangableForVmInPoolEntityModel<Boolean>());
        getProvisioningClone_IsSelected().getEntityChangedEvent().addListener(this);

        setProvisioningThin_IsSelected(new NotChangableForVmInPoolEntityModel<Boolean>());
        getProvisioningThin_IsSelected().getEntityChangedEvent().addListener(this);

        setCpuPinning(new NotChangableForVmInPoolEntityModel<String>());
        getCpuPinning().setEntity("");
        getCpuPinning().setIsChangable(false);

        setCpuSharesAmount(new NotChangableForVmInPoolEntityModel<Integer>());
        getCpuSharesAmount().setIsChangable(false);

        setCpuSharesAmountSelection(new NotChangableForVmInPoolListModel<CpuSharesAmount>());
        getCpuSharesAmountSelection().setItems(Arrays.asList(CpuSharesAmount.values()));
        getCpuSharesAmountSelection().getEntityChangedEvent().addListener(this);
        getCpuSharesAmountSelection().getSelectedItemChangedEvent().addListener(this);
        getCpuSharesAmountSelection().setSelectedItem(CpuSharesAmount.DISABLED);

        setIsSoundcardEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        getIsSoundcardEnabled().setEntity(false);
        getIsSoundcardEnabled().setIsChangable(false);

        setIsSingleQxlEnabled(new NotChangableForVmInPoolEntityModel<Boolean>());
        getBehavior().enableSinglePCI(false);

        selectSsoMethod(SsoMethod.GUEST_AGENT);

        setEditingEnabled(new EntityModel<Boolean>());
        getEditingEnabled().setEntity(true);
    }

    public void initialize(SystemTreeItemModel SystemTreeSelectedItem)
    {
        super.initialize();

        setHash(getHashName() + new Date());

        getMemSize().setEntity(256);
        getMinAllocatedMemory().setEntity(256);
        getIsStateless().setEntity(false);
        getIsRunAndPause().setEntity(false);
        getIsSmartcardEnabled().setEntity(false);
        isConsoleDeviceEnabled.setEntity(false);
        getIsHighlyAvailable().setEntity(false);
        getIsAutoAssign().setEntity(true);
        getIsTemplatePublic().setEntity(true);
        getBehavior().enableSinglePCI(false);

        getHostCpu().setEntity(false);
        getMigrationMode().setIsChangable(true);

        getCdImage().setIsChangable(false);

        initDisplayProtocol();
        initFirstBootDevice();
        initNumOfMonitors();
        initAllowConsoleReconnect();
        initMinimalVmMemSize();
        initMaximalVmMemSize32OS();
        initMigrationMode();
        initVncKeyboardLayout();

        behavior.initialize(SystemTreeSelectedItem);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(Frontend.getInstance().getQueryStartedEventDefinition())
                && ObjectUtils.objectsEqual(Frontend.getInstance().getCurrentContext(), getHash()))
        {
            frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.getInstance().getQueryCompleteEventDefinition())
                && ObjectUtils.objectsEqual(Frontend.getInstance().getCurrentContext(), getHash()))
        {
            frontend_QueryComplete();
        }
        else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition))
        {
            if (sender == getVmType()) {
                vmTypeChanged();
            } else if (sender == getDataCenterWithClustersList())
            {
                dataCenterWithClusterSelectedItemChanged(sender, args);
                updateDisplayProtocol();
                initUsbPolicy();
            }
            else if (sender == getTemplate())
            {
                template_SelectedItemChanged(sender, args);
            }
            else if (sender == getTimeZone())
            {
                timeZone_SelectedItemChanged(sender, args);
            }
            else if (sender == getDefaultHost())
            {
                defaultHost_SelectedItemChanged(sender, args);
            }
            else if (sender == getOSType()) {
                oSType_SelectedItemChanged(sender, args);
                getBehavior().oSType_SelectedItemChanged();
                getVmInitModel().osTypeChanged(getOSType().getSelectedItem());
                updateDisplayProtocol();
                initUsbPolicy();
            }
            else if (sender == getFirstBootDevice())
            {
                firstBootDevice_SelectedItemChanged(sender, args);
            }
            else if (sender == getDisplayProtocol())
            {
                displayProtocol_SelectedItemChanged(sender, args);
                initUsbPolicy();
            }
            else if (sender == getNumOfSockets())
            {
                numOfSockets_EntityChanged(sender, args);
            }
            else if (sender == getCoresPerSocket())
            {
                coresPerSocket_EntityChanged(sender, args);
            }
            else if (sender == getMigrationMode())
            {
                behavior.updateUseHostCpuAvailability();
                behavior.updateCpuPinningVisibility();
                behavior.updateHaAvailability();
            }
            else if (sender == getCpuSharesAmountSelection())
            {
                behavior.updateCpuSharesAmountChangeability();
            }
            else if (sender == getBaseTemplate()) {
                behavior.baseTemplateSelectedItemChanged();
            }
        }
        else if (ev.matchesDefinition(EntityModel.entityChangedEventDefinition))
        {
            if (sender == getVmInitEnabled()) {
                vmInitEnabledChanged();
            }
            else if (sender == getMemSize())
            {
                memSize_EntityChanged(sender, args);
            }
            else if (sender == getTotalCPUCores())
            {
                totalCPUCores_EntityChanged(sender, args);
            }
            else if (sender == getIsAutoAssign())
            {
                behavior.updateUseHostCpuAvailability();
                behavior.updateCpuPinningVisibility();
                behavior.updateHaAvailability();
            }
            else if (sender == getProvisioning())
            {
                provisioning_SelectedItemChanged(sender, args);
            }
            else if (sender == getProvisioningThin_IsSelected())
            {
                if (getProvisioningThin_IsSelected().getEntity()) {
                    getProvisioning().setEntity(false);
                }
            }
            else if (sender == getProvisioningClone_IsSelected())
            {
                if (getProvisioningClone_IsSelected().getEntity()) {
                    getProvisioning().setEntity(true);
                }
            } else if (sender == getWatchdogModel()) {
                WatchdogModel_EntityChanged(sender, args);
            } else if (sender == getIsHighlyAvailable()) {
                behavior.updateMigrationAvailability();
            } else if (sender == getOverrideMigrationDowntime()) {
                overrideMigrationDowntimeChanged();
            }

        }
    }

    private void vmInitEnabledChanged() {
        if(!getVmInitEnabled().getEntity()) {
            getSysprepEnabled().setEntity(false);
            getCloudInitEnabled().setEntity(false);
        } else {
            getSysprepEnabled().setEntity(getIsWindowsOS());
            // for the "other" also use cloud init
            getCloudInitEnabled().setEntity(!getIsWindowsOS());
        }
    }

    private void vmTypeChanged() {
        behavior.vmTypeChanged(getVmType().getSelectedItem());
    }

    private void WatchdogModel_EntityChanged(Object sender, EventArgs args) {
        if ("".equals(getWatchdogModel().getEntity())) {
            getWatchdogAction().setIsChangable(false);
            getWatchdogAction().setSelectedItem(""); //$NON-NLS-1$
        } else {
            getWatchdogAction().setIsChangable(true);
        }
    }

    private int queryCounter;

    private void frontend_QueryStarted()
    {
        queryCounter++;
        if (getProgress() == null)
        {
            startProgress(null);
        }
    }

    private void frontend_QueryComplete()
    {
        queryCounter--;
        if (queryCounter == 0)
        {
            stopProgress();
        }
    }

    protected void initNumOfMonitors()
    {
        AsyncDataProvider.getNumOfMonitorList(new AsyncQuery(this,
                                                             new INewAsyncCallback() {
                                                                 @Override
                                                                 public void onSuccess(Object target, Object returnValue) {

                                                                     UnitVmModel model = (UnitVmModel) target;
                                                                     Integer oldNumOfMonitors = null;
                                                                     if (model.getNumOfMonitors().getSelectedItem() != null) {
                                                                         oldNumOfMonitors = model.getNumOfMonitors().getSelectedItem();
                                                                     }
                                                                     ArrayList<Integer> numOfMonitors = (ArrayList<Integer>) returnValue;
                                                                     model.getNumOfMonitors().setItems(numOfMonitors);
                                                                     if (oldNumOfMonitors != null) {
                                                                         model.getNumOfMonitors().setSelectedItem(oldNumOfMonitors);
                                                                     }

                                                                 }
                                                             }, getHash()));

    }

    protected void initAllowConsoleReconnect() {
        getAllowConsoleReconnect().setEntity(getVmType().getSelectedItem() == VmType.Server);
    }

    private void initUsbPolicy() {
        VDSGroup cluster = getSelectedCluster();
        Integer osType = getOSType().getSelectedItem();
        DisplayType displayType = (getDisplayProtocol().getSelectedItem() != null ?
                getDisplayProtocol().getSelectedItem().getEntity() : null);

        if (osType == null || cluster == null || displayType == null) {
            return;
        }

        getUsbPolicy().setIsChangable(true);
        if (Version.v3_1.compareTo(cluster.getcompatibility_version()) > 0) {
            if (AsyncDataProvider.isWindowsOsType(osType)) {
                getUsbPolicy().setItems(Arrays.asList(
                        UsbPolicy.DISABLED,
                        UsbPolicy.ENABLED_LEGACY
                        ));
            } else {
                getUsbPolicy().setItems(Arrays.asList(UsbPolicy.DISABLED));
                getUsbPolicy().setSelectedItem(UsbPolicy.DISABLED);
                getUsbPolicy().setIsChangable(false);
            }
        }

        if (Version.v3_1.compareTo(cluster.getcompatibility_version()) <= 0) {
            if (AsyncDataProvider.isLinuxOsType(osType)) {
                getUsbPolicy().setItems(Arrays.asList(
                        UsbPolicy.DISABLED,
                        UsbPolicy.ENABLED_NATIVE
                        ));
            } else {
                getUsbPolicy().setItems(
                        Arrays.asList(
                                UsbPolicy.DISABLED,
                                UsbPolicy.ENABLED_LEGACY,
                                UsbPolicy.ENABLED_NATIVE
                                ));
            }
        }

        if (displayType != DisplayType.qxl) {
            getUsbPolicy().setIsChangable(false);
        }

        getUsbPolicy().setSelectedItem(UsbPolicy.DISABLED);
    }

    private void initMinimalVmMemSize()
    {
        AsyncDataProvider.getMinimalVmMemSize(new AsyncQuery(this,
                                                             new INewAsyncCallback() {
                                                                 @Override
                                                                 public void onSuccess(Object target, Object returnValue) {

                                                                     UnitVmModel vmModel = (UnitVmModel) target;
                                                                     vmModel.set_MinMemSize((Integer) returnValue);

                                                                 }
                                                             }, getHash()));
    }

    private void initMaximalVmMemSize32OS()
    {
        AsyncDataProvider.getMaximalVmMemSize32OS(new AsyncQuery(this,
                                                                 new INewAsyncCallback() {
                                                                     @Override
                                                                     public void onSuccess(Object target, Object returnValue) {

                                                                         UnitVmModel vmModel = (UnitVmModel) target;
                                                                         vmModel.set_MaxMemSize32((Integer) returnValue);

                                                                     }
                                                                 }, getHash()));
    }

    private void updateMigrationOptions()
    {
        DataCenterWithCluster dataCenterWithCluster =
                (DataCenterWithCluster) getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return;
        }

        VDSGroup cluster = dataCenterWithCluster.getCluster();

        Boolean isMigrationSupported =
                AsyncDataProvider.isMigrationSupported(cluster.getArchitecture(),
                        cluster.getcompatibility_version());

        if (isMigrationSupported) {
            getMigrationMode().setItems(Arrays.asList(MigrationSupport.values()));
        } else {
            getMigrationMode().setItems(Arrays.asList(MigrationSupport.PINNED_TO_HOST));
        }
    }

    private void updateMaximalVmMemSize()
    {
        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return;
        }

        VDSGroup cluster = dataCenterWithCluster.getCluster();

        if (cluster != null)
        {
            AsyncDataProvider.getMaximalVmMemSize64OS(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            UnitVmModel vmModel = (UnitVmModel) target;
                            vmModel.set_MaxMemSize64((Integer) returnValue);

                        }
                    }, getHash()), cluster.getcompatibility_version().toString());
        }
    }

    private void initDisplayProtocol()
    {
        getDisplayProtocol().getSelectedItemChangedEvent().addListener(this);
    }

    private void updateDisplayProtocol()
    {
        DisplayType oldDisplayProtocolOption = null;

        if (getDisplayProtocol().getSelectedItem() != null) {
            oldDisplayProtocolOption = getDisplayProtocol().getSelectedItem().getEntity();
        }

        VDSGroup cluster = getSelectedCluster();
        Integer osType = getOSType().getSelectedItem();

        if (cluster == null || osType == null) {
            return;
        }

        List<EntityModel<DisplayType>> displayProtocolOptions = new ArrayList<EntityModel<DisplayType>>();
        List<DisplayType> displayTypes = AsyncDataProvider.getDisplayTypes(osType, cluster.getcompatibility_version());

        if (displayTypes.contains(DisplayType.vnc)) {
            EntityModel<DisplayType> vncProtocol = new EntityModel<DisplayType>();
            vncProtocol.setTitle(ConstantsManager.getInstance().getConstants().VNCTitle());
            vncProtocol.setEntity(DisplayType.vnc);
            displayProtocolOptions.add(vncProtocol);
        }

        if (displayTypes.contains(DisplayType.qxl)) {
            EntityModel<DisplayType> spiceProtocol = new EntityModel<DisplayType>();
            spiceProtocol.setTitle(ConstantsManager.getInstance().getConstants().spiceTitle());
            spiceProtocol.setEntity(DisplayType.qxl);
            displayProtocolOptions.add(spiceProtocol);
        }

        getDisplayProtocol().setItems(displayProtocolOptions);

        behavior.postDisplayTypeItemChanged(oldDisplayProtocolOption);
    }

    private void initFirstBootDevice()
    {
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().hardDiskTitle());
        tempVar.setEntity(BootSequence.C);
        EntityModel hardDiskOption = tempVar;

        List<EntityModel<BootSequence>> firstBootDeviceItems = new ArrayList<EntityModel<BootSequence>>();
        firstBootDeviceItems.add(hardDiskOption);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cdromTitle());
        tempVar2.setEntity(BootSequence.D);
        firstBootDeviceItems.add(tempVar2);
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().networkPXETitle());
        tempVar3.setEntity(BootSequence.N);
        firstBootDeviceItems.add(tempVar3);
        getFirstBootDevice().setItems(firstBootDeviceItems);
        getFirstBootDevice().setSelectedItem(hardDiskOption);
    }

    private void initMigrationMode() {
        getMigrationMode().setItems(Arrays.asList(MigrationSupport.values()));
    }

    private void initVncKeyboardLayout() {

        final List<String> layouts =
                (List<String>) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.VncKeyboardLayoutValidValues);
        final ArrayList<String> vncKeyboardLayoutItems = new ArrayList<String>();
        vncKeyboardLayoutItems.add(null); // null value means the global VncKeyboardLayout from vdc_options will be used
        vncKeyboardLayoutItems.addAll(layouts);
        getVncKeyboardLayout().setItems(vncKeyboardLayoutItems);

        getVncKeyboardLayout().setIsAvailable(isVncSelected());
    }

    private void dataCenterWithClusterSelectedItemChanged(Object sender, EventArgs args)
    {
        behavior.dataCenterWithClusterSelectedItemChanged();

        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster != null && dataCenterWithCluster.getDataCenter() != null) {
            getDisksAllocationModel().setQuotaEnforcementType(dataCenterWithCluster.getDataCenter()
                    .getQuotaEnforcementType());
        }

        updateMigrationOptions();
        updateMaximalVmMemSize();
        handleQxlClusterLevel();

        updateWatchdogModels();
    }

    private void handleQxlClusterLevel() {
        // Enable Single PCI only on cluster 3.3 and high and on Linux OS
        boolean isLinux = getIsLinuxOS();
        boolean isQxl = getDisplayType() == DisplayType.qxl;
        boolean clusterSupportsSinglePci = getSelectedCluster() != null &&
        Version.v3_3.compareTo(getSelectedCluster().getcompatibility_version()) <= 0;

        getBehavior().enableSinglePCI(isLinux && isQxl && clusterSupportsSinglePci);
    }

    private void template_SelectedItemChanged(Object sender, EventArgs args)
    {
        behavior.template_SelectedItemChanged();
        behavior.updateMigrationForLocalSD();
    }

    private void timeZone_SelectedItemChanged(Object sender, EventArgs args)
    {
    }

    private void defaultHost_SelectedItemChanged(Object sender, EventArgs args)
    {
        behavior.defaultHost_SelectedItemChanged();
    }

    private void oSType_SelectedItemChanged(Object sender, EventArgs args)
    {
        Integer osType = getOSType().getSelectedItem();

        setIsWindowsOS(AsyncDataProvider.isWindowsOsType(osType));
        setIsLinuxOS(AsyncDataProvider.isLinuxOsType(osType));

        getInitrd_path().setIsChangable(getIsLinuxOS());
        getInitrd_path().setIsAvailable(getIsLinuxOS());

        getKernel_path().setIsChangable(getIsLinuxOS());
        getKernel_path().setIsAvailable(getIsLinuxOS());

        getKernel_parameters().setIsChangable(getIsLinuxOS());
        getKernel_parameters().setIsAvailable(getIsLinuxOS());

        getDomain().setIsChangable(getIsWindowsOS());

        getBehavior().updateDefaultTimeZone();


        handleQxlClusterLevel();

        updateWatchdogModels(osType);

        vmInitEnabledChanged();

    }

    private void updateWatchdogModels() {
        updateWatchdogModels((Integer) getOSType().getSelectedItem());
    }

    private void updateWatchdogModels(Integer osType) {
        VDSGroup cluster = getSelectedCluster();
        if (osType != null && cluster != null && getWatchdogModel() != null) {
            AsyncQuery asyncQuery = new AsyncQuery();
            asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {
                    updateWatchdogItems((HashSet<VmWatchdogType>) ((VdcQueryReturnValue) returnValue)
                            .getReturnValue());
                }
            };
            AsyncDataProvider.getVmWatchdogTypes(osType,
                                                 cluster.getcompatibility_version(), asyncQuery);
        }
    }

    private void updateWatchdogItems(Set<VmWatchdogType> vmWatchdogTypes) {
        List<String> watchDogModels = new ArrayList<String>();
        for (VmWatchdogType vmWatchdogType : vmWatchdogTypes) {
            watchDogModels.add(EnumTranslator.createAndTranslate(vmWatchdogType));
        }

        watchDogModels.add(0, null);
        String oldWatchdogSelected = (String) getWatchdogModel().getSelectedItem();
        getWatchdogModel().setItems(watchDogModels);

        if (watchDogModels.contains(oldWatchdogSelected)) {
            getWatchdogModel().setSelectedItem(oldWatchdogSelected);
        }
    }

    private void firstBootDevice_SelectedItemChanged(Object sender, EventArgs args)
    {
        EntityModel<BootSequence> entityModel = getFirstBootDevice().getSelectedItem();
        BootSequence firstDevice = entityModel.getEntity();

        List<EntityModel<BootSequence>> list = new ArrayList<EntityModel<BootSequence>>();
        for (EntityModel<BootSequence> item : getFirstBootDevice().getItems())
        {
            if (item.getEntity() != firstDevice)
            {
                list.add(item);
            }
        }

        EntityModel<BootSequence> tempVar = new EntityModel<BootSequence>();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().noneTitle());
        EntityModel<BootSequence> noneOption = tempVar;

        list.add(0, noneOption);

        getSecondBootDevice().setItems(list);
        getSecondBootDevice().setSelectedItem(noneOption);
    }

    private void provisioning_SelectedItemChanged(Object sender, EventArgs args)
    {
        behavior.provisioning_SelectedItemChanged();
    }

    private void overrideMigrationDowntimeChanged() {
        Boolean entity = getOverrideMigrationDowntime().getEntity();
        getMigrationDowntime().setIsChangable(Boolean.TRUE.equals(entity));
    }

    private DisplayType getDisplayType() {
        EntityModel<DisplayType> entityModel = getDisplayProtocol().getSelectedItem();
        if (entityModel == null)
        {
            return null;
        }
        return entityModel.getEntity();
    }

    private void displayProtocol_SelectedItemChanged(Object sender, EventArgs args)
    {
        if (getDisplayType() == null)
        {
            return;
        }
        DisplayType type = getDisplayType();

        if (type == DisplayType.vnc)
        {
            getUsbPolicy().setSelectedItem(org.ovirt.engine.core.common.businessentities.UsbPolicy.DISABLED);
            getIsSmartcardEnabled().setEntity(false);
        }

        handleQxlClusterLevel();

        getUsbPolicy().setIsChangable(type == DisplayType.qxl);
        getIsSmartcardEnabled().setIsChangable(type == DisplayType.qxl);

        getVncKeyboardLayout().setIsAvailable(type == DisplayType.vnc);

        updateNumOfMonitors();
    }

    private void memSize_EntityChanged(Object sender, EventArgs args)
    {
        behavior.updateMinAllocatedMemory();
    }

    private void numOfSockets_EntityChanged(Object sender, EventArgs args)
    {
        behavior.numOfSocketChanged();
    }

    private void totalCPUCores_EntityChanged(Object sender, EventArgs args) {
        // do not listen on changes while the totalCpuCoresChanged is adjusting them
        getNumOfSockets().getSelectedItemChangedEvent().removeListener(this);
        getTotalCPUCores().getEntityChangedEvent().removeListener(this);
        getCoresPerSocket().getSelectedItemChangedEvent().removeListener(this);

        behavior.totalCpuCoresChanged();

        // start listening again
        getTotalCPUCores().getEntityChangedEvent().addListener(this);
        getNumOfSockets().getSelectedItemChangedEvent().addListener(this);
        getCoresPerSocket().getSelectedItemChangedEvent().addListener(this);
    }

    private void coresPerSocket_EntityChanged(Object sender, EventArgs args) {
        behavior.coresPerSocketChanged();
    }

    private boolean isVncSelected() {
        boolean isVnc = false;

        if (getDisplayProtocol().getSelectedItem() != null)
        {
            DisplayType displayType = getDisplayProtocol().getSelectedItem().getEntity();
            isVnc = displayType == DisplayType.vnc;
        }

        return isVnc;
    }

    private void updateNumOfMonitors()
    {
        if (isVncSelected())
        {
            getNumOfMonitors().setSelectedItem(1);
            getNumOfMonitors().setIsChangable(false);
        } else {
            getNumOfMonitors().setIsChangable(true);
        }
    }

    public BootSequence getBootSequence()
    {
        EntityModel<BootSequence> firstSelectedItem = getFirstBootDevice().getSelectedItem();
        EntityModel<BootSequence> secondSelectedItem = getSecondBootDevice().getSelectedItem();

        String firstSelectedString =
                firstSelectedItem.getEntity() == null ? "" : firstSelectedItem.getEntity().toString(); //$NON-NLS-1$
        String secondSelectedString =
                secondSelectedItem.getEntity() == null ? "" : secondSelectedItem.getEntity().toString(); //$NON-NLS-1$

        return BootSequence.valueOf(firstSelectedString + secondSelectedString);
    }

    public void setBootSequence(BootSequence value)
    {
        ArrayList<BootSequence> items = new ArrayList<BootSequence>();
        for (char a : value.toString().toCharArray())
        {
            items.add(BootSequence.valueOf(String.valueOf(a)));
        }

        EntityModel<BootSequence> firstBootDevice = null;
        for (EntityModel<BootSequence> item : getFirstBootDevice().getItems())
        {
            if (item.getEntity() == Linq.firstOrDefault(items))
            {
                firstBootDevice = item;
            }
        }
        getFirstBootDevice().setSelectedItem(firstBootDevice);

        Iterable<EntityModel<BootSequence>> secondDeviceOptions = getSecondBootDevice().getItems();

        if (items.size() > 1)
        {
            BootSequence last = items.get(items.size() - 1);
            for (EntityModel<BootSequence> a : secondDeviceOptions)
            {
                if (a.getEntity() != null && a.getEntity() == last)
                {
                    getSecondBootDevice().setSelectedItem(a);
                    break;
                }
            }
        }
        else
        {
            for (EntityModel<BootSequence> a : secondDeviceOptions)
            {
                if (a.getEntity() == null)
                {
                    getSecondBootDevice().setSelectedItem(a);
                    break;
                }
            }
        }
    }

    public void setDataCentersAndClusters(UnitVmModel model,
            List<StoragePool> dataCenters,
            List<VDSGroup> clusters,
            Guid selectedCluster) {

        if (model.getBehavior().getSystemTreeSelectedItem() != null
                && model.getBehavior().getSystemTreeSelectedItem().getType() != SystemTreeItemType.System) {
            setupDataCenterWithClustersFromSystemTree(model, dataCenters, clusters, selectedCluster);
        } else {
            setupDataCenterWithClusters(model, dataCenters, clusters, selectedCluster);
        }

    }

    protected void setupDataCenterWithClustersFromSystemTree(UnitVmModel model,
            List<StoragePool> dataCenters,
            List<VDSGroup> clusters,
            Guid selectedCluster) {

        StoragePool dataCenter = getDataCenterAccordingSystemTree(model, dataCenters);

        // the dataCenters are the entities just downloaded from server while the dataCenter can be a cached one from the system tree
        dataCenter = dataCenter == null ? null : findDataCenterById(dataCenters, dataCenter.getId());

        List<VDSGroup> possibleClusters = getClusterAccordingSystemTree(model, clusters);
        if (dataCenter == null || possibleClusters == null) {
            getDataCenterWithClustersList().setIsChangable(false);
            return;
        }

        List<DataCenterWithCluster> dataCentersWithClusters =
                new ArrayList<DataCenterWithCluster>();

        for (VDSGroup cluster : possibleClusters) {
            if (cluster.getStoragePoolId() != null && cluster.getStoragePoolId().equals(dataCenter.getId())) {
                dataCentersWithClusters.add(new DataCenterWithCluster(dataCenter, cluster));
            }
        }
        getDataCenterWithClustersList().setItems(dataCentersWithClusters);

        selectDataCenterWithCluster(model, selectedCluster, dataCentersWithClusters);
    }

    protected void setupDataCenterWithClusters(UnitVmModel model,
            List<StoragePool> dataCenters,
            List<VDSGroup> clusters,
            Guid selectedCluster) {

        Map<Guid, List<VDSGroup>> dataCenterToCluster = new HashMap<Guid, List<VDSGroup>>();
        for (VDSGroup cluster : clusters) {
            if (cluster.getStoragePoolId() == null) {
                continue;
            }

            if (!dataCenterToCluster.containsKey(cluster.getStoragePoolId())) {
                dataCenterToCluster.put(cluster.getStoragePoolId(), new ArrayList<VDSGroup>());
            }
            dataCenterToCluster.get(cluster.getStoragePoolId()).add(cluster);
        }

        List<DataCenterWithCluster> dataCentersWithClusters =
                new ArrayList<DataCenterWithCluster>();

        for (StoragePool dataCenter : dataCenters) {
            if (dataCenterToCluster.containsKey(dataCenter.getId())) {
                for (VDSGroup cluster : dataCenterToCluster.get(dataCenter.getId())) {
                    dataCentersWithClusters.add(new DataCenterWithCluster(dataCenter, cluster));
                }
            }
        }
        getDataCenterWithClustersList().setItems(dataCentersWithClusters);

        selectDataCenterWithCluster(model, selectedCluster, dataCentersWithClusters);
    }

    protected void selectDataCenterWithCluster(UnitVmModel model,
            Guid selectedCluster,
            List<DataCenterWithCluster> dataCentersWithClusters) {
        if (selectedCluster == null) {
            getDataCenterWithClustersList().setSelectedItem(Linq.firstOrDefault(dataCentersWithClusters));
        } else {
            model.getDataCenterWithClustersList().setSelectedItem(Linq.firstOrDefault(dataCentersWithClusters,
                    new Linq.DataCenterWithClusterAccordingClusterPredicate(selectedCluster)));
        }
    }

    private StoragePool getDataCenterAccordingSystemTree(UnitVmModel model, List<StoragePool> list) {
        if (model.getBehavior().getSystemTreeSelectedItem() != null
                && model.getBehavior().getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
        {
            switch (model.getBehavior().getSystemTreeSelectedItem().getType())
            {
            case Templates:
            case DataCenter:
                return (StoragePool) model.getBehavior().getSystemTreeSelectedItem().getEntity();
            case Cluster:
            case Cluster_Gluster:
            case VMs:
                VDSGroup cluster = (VDSGroup) model.getBehavior().getSystemTreeSelectedItem().getEntity();
                if (cluster.supportsVirtService()) {
                    return findDataCenterById(list, cluster.getStoragePoolId());
                }
                break;

            case Host:
                VDS host = (VDS) model.getBehavior().getSystemTreeSelectedItem().getEntity();
                return findDataCenterById(list, host.getStoragePoolId());

            case Storage:
                StorageDomain storage = (StorageDomain) model.getBehavior().getSystemTreeSelectedItem().getEntity();
                return findDataCenterById(list, storage.getStoragePoolId());
            }
        }
        return null;
    }

    private StoragePool findDataCenterById(List<StoragePool> list, Guid id) {
        if (id == null) {
            return null;
        }

        for (StoragePool dc : list) {
            if (dc.getId().equals(id)) {
                return dc;
            }
        }

        return null;
    }

    private List<VDSGroup> getClusterAccordingSystemTree(UnitVmModel model, List<VDSGroup> clusters) {
        if (behavior.getSystemTreeSelectedItem() != null
                && behavior.getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
        {
            switch (model.getBehavior().getSystemTreeSelectedItem().getType())
            {
            case Cluster:
            case VMs:
                VDSGroup cluster = (VDSGroup) behavior.getSystemTreeSelectedItem().getEntity();
                return Arrays.asList(cluster);

            case Host:
                VDS host = (VDS) behavior.getSystemTreeSelectedItem().getEntity();
                for (VDSGroup iterCluster : clusters) {
                    if (iterCluster.getId().equals(host.getVdsGroupId())) {
                        return Arrays.asList(iterCluster);
                    }
                }
                break;
            default:
                return clusters;
            }
        }

        return null;
    }

    public boolean validate() {
        getDataCenterWithClustersList().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getMemSize().validateEntity(new IValidation[] { new ByteSizeValidation() });
        getMinAllocatedMemory().validateEntity(new IValidation[] { new ByteSizeValidation() });
        getOSType().validateSelectedItem(new NotEmptyValidation[] { new NotEmptyValidation() });

        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();

        StoragePool dataCenter =
                dataCenterWithCluster == null ? null : dataCenterWithCluster.getDataCenter();
        if (dataCenter != null && dataCenter.getQuotaEnforcementType() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT) {
            getQuota().validateSelectedItem(new IValidation[] { new NotEmptyQuotaValidation() });
        }

        getTotalCPUCores().validateEntity(new IValidation[] {
                new NotEmptyValidation(),
                new IntegerValidation(1, behavior.maxCpus),
                new TotalCpuCoresComposableValidation() });

        if (getOSType().getIsValid()) {
            Integer osType = getOSType().getSelectedItem();
            getName().validateEntity(
                    new IValidation[] {
                            new NotEmptyValidation(),
                            new LengthValidation(
                                    (getBehavior() instanceof TemplateVmModelBehavior || getBehavior() instanceof NewTemplateVmModelBehavior)
                                            ? VM_TEMPLATE_NAME_MAX_LIMIT
                                            : AsyncDataProvider.isWindowsOsType(osType) ? AsyncDataProvider.getMaxVmNameLengthWin()
                                                    : AsyncDataProvider.getMaxVmNameLengthNonWin()),
                            isPoolTabValid ? new PoolNameValidation() : new I18NNameValidation()
                    });

            getDescription().validateEntity(
                    new IValidation[] {
                            new LengthValidation(DESCRIPTION_MAX_LIMIT),
                            new SpecialAsciiI18NOrNoneValidation()
                    });

            AsyncQuery asyncQuery = new AsyncQuery();
            asyncQuery.setModel(this);
            asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {
                    validateMemorySize(getMemSize(), (Integer)((VdcQueryReturnValue)returnValue).getReturnValue(), _minMemSize);
                    if (!(((UnitVmModel)model).getBehavior() instanceof TemplateVmModelBehavior)) {
                        // Minimum 'Physical Memory Guaranteed' is 1MB
                        validateMemorySize(getMinAllocatedMemory(), getMemSize().getEntity(), 1);
                    }
                }
            };

            if (getSelectedCluster() != null) {
                AsyncDataProvider.getOsMaxRam(osType, getSelectedCluster().getcompatibility_version(), asyncQuery);
            }

            getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });
        }

        if (getIsAutoAssign().getEntity() != null && getIsAutoAssign().getEntity() == false) {
            getDefaultHost().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        } else {
            getDefaultHost().setIsValid(true);
        }

        getTemplate().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getDisksAllocationModel().validateEntity(new IValidation[] {});

        getCdImage().setIsValid(true);
        if (getCdImage().getIsChangable()) {
            getCdImage().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        }

        getMigrationDowntime().validateEntity(new IValidation[] { new NotNullIntegerValidation(0, Integer.MAX_VALUE) });

        if (getIsLinuxOS()) {
            getKernel_path().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getInitrd_path().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });
            getKernel_parameters().validateEntity(new IValidation[] { new NoTrimmingWhitespacesValidation() });

            // initrd path and kernel params require kernel path to be filled
            if (StringHelper.isNullOrEmpty(getKernel_path().getEntity())) {
                final UIConstants constants = ConstantsManager.getInstance().getConstants();

                if (!StringHelper.isNullOrEmpty(getInitrd_path().getEntity())) {
                    getInitrd_path().getInvalidityReasons().add(constants.initrdPathInvalid());
                    getInitrd_path().setIsValid(false);
                    getKernel_path().getInvalidityReasons().add(constants.initrdPathInvalid());
                    getKernel_path().setIsValid(false);
                }

                if (!StringHelper.isNullOrEmpty(getKernel_parameters().getEntity())) {
                    getKernel_parameters().getInvalidityReasons().add(constants.kernelParamsInvalid());
                    getKernel_parameters().setIsValid(false);
                    getKernel_path().getInvalidityReasons().add(constants.kernelParamsInvalid());
                    getKernel_path().setIsValid(false);
                }
            }
        }

        if (getCpuSharesAmount().getIsAvailable()) {
            getCpuSharesAmount().validateEntity(new IValidation[] {new NotEmptyValidation()
                    , new IntegerValidation(0, 262144)});
        }

        boolean customPropertySheetValid = getCustomPropertySheet().validate();

        if (getSerialNumberPolicy().getSelectedSerialNumberPolicy() == SerialNumberPolicy.CUSTOM) {
            getSerialNumberPolicy().getCustomSerialNumber().validateEntity(new IValidation[] { new NotEmptyValidation() });
        } else {
            getSerialNumberPolicy().getCustomSerialNumber().setIsValid(true);
        }

        setIsBootSequenceTabValid(true);
        setIsAllocationTabValid(getIsBootSequenceTabValid());
        setIsDisplayTabValid(getIsAllocationTabValid());
        setIsFirstRunTabValid(getIsDisplayTabValid());
        setIsGeneralTabValid(getIsFirstRunTabValid());

        boolean behaviorValid = behavior.validate();
        setIsGeneralTabValid(getName().getIsValid() && getDescription().getIsValid() && getComment().getIsValid()
                && getDataCenterWithClustersList().getIsValid()
                && getTemplate().getIsValid() && getMemSize().getIsValid()
                && getMinAllocatedMemory().getIsValid());

        setIsFirstRunTabValid(getDomain().getIsValid() && getTimeZone().getIsValid());
        setIsDisplayTabValid(getUsbPolicy().getIsValid() && getNumOfMonitors().getIsValid() && getSpiceProxy().getIsValid());
        setIsHostTabValid(getDefaultHost().getIsValid() && getMigrationDowntime().getIsValid());
        setIsAllocationTabValid(getDisksAllocationModel().getIsValid() && getMinAllocatedMemory().getIsValid()
                && getCpuSharesAmount().getIsValid());
        setIsBootSequenceTabValid(getCdImage().getIsValid() && getKernel_path().getIsValid());
        setIsCustomPropertiesTabValid(customPropertySheetValid);

        return getName().getIsValid() && getDescription().getIsValid() && getDataCenterWithClustersList().getIsValid()
                && getDisksAllocationModel().getIsValid() && getTemplate().getIsValid() && getComment().getIsValid()
                && getDefaultHost().getIsValid() && getMemSize().getIsValid() && getMinAllocatedMemory().getIsValid()
                && getNumOfMonitors().getIsValid() && getDomain().getIsValid() && getUsbPolicy().getIsValid()
                && getTimeZone().getIsValid() && getOSType().getIsValid() && getCdImage().getIsValid()
                && getKernel_path().getIsValid()
                && getInitrd_path().getIsValid()
                && getKernel_parameters().getIsValid()
                && getCpuSharesAmount().getIsValid()
                && behaviorValid
                && customPropertySheetValid && getQuota().getIsValid()
                && getMigrationDowntime().getIsValid()
                && getSerialNumberPolicy().getCustomSerialNumber().getIsValid();

    }

    public SsoMethod extractSelectedSsoMethod() {
        return Boolean.TRUE.equals(getSsoMethodGuestAgent().getEntity())
                ? SsoMethod.GUEST_AGENT
                : SsoMethod.NONE;
    }

    public void selectSsoMethod(SsoMethod ssoMethod) {
        getSsoMethodNone().setEntity(SsoMethod.NONE.equals(ssoMethod));
        getSsoMethodGuestAgent().setEntity(SsoMethod.GUEST_AGENT.equals(ssoMethod));
    }

    class TotalCpuCoresComposableValidation implements IValidation {

        @Override
        public ValidationResult validate(Object value) {
            boolean isOk = behavior.isNumOfSocketsCorrect(Integer.parseInt(getTotalCPUCores().getEntity()));
            ValidationResult res = new ValidationResult();
            res.setSuccess(isOk);
            res.setReasons(Arrays.asList(ConstantsManager.getInstance()
                    .getMessages()
                    .incorrectVCPUNumber()));
            return res;

        }

    }

    private void validateMemorySize(EntityModel model, int maxMemSize, int minMemSize)
    {
        boolean isValid = false;

        int memSize = (Integer) model.getEntity();

        if (memSize == 0)
        {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .memSizeBetween(minMemSize, maxMemSize));
        }
        else if (memSize > maxMemSize)
        {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .maxMemSizeIs(maxMemSize));
        }
        else if (memSize < minMemSize)
        {
            model.getInvalidityReasons().add(ConstantsManager.getInstance()
                    .getMessages()
                    .minMemSizeIs(minMemSize));
        }
        else
        {
            isValid = true;
        }

        model.setIsValid(isValid);
    }

    private NotChangableForVmInPoolListModel<EntityModel<VmPoolType>> poolType;

    public ListModel<EntityModel<VmPoolType>> getPoolType()
    {
        return poolType;
    }

    protected void setPoolType(NotChangableForVmInPoolListModel<EntityModel<VmPoolType>> value)
    {
        poolType = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> numOfDesktops;

    public EntityModel<Integer> getNumOfDesktops()
    {
        return numOfDesktops;
    }

    protected void setNumOfDesktops(NotChangableForVmInPoolEntityModel<Integer> value)
    {
        numOfDesktops = value;
    }

    private NotChangableForVmInPoolEntityModel<Integer> assignedVms;

    public EntityModel<Integer> getAssignedVms()
    {
        return assignedVms;
    }

    public void setAssignedVms(NotChangableForVmInPoolEntityModel<Integer> value)
    {
        assignedVms = value;
    }

    private boolean isPoolTabValid;

    public boolean getIsPoolTabValid()
    {
        return isPoolTabValid;
    }

    public void setIsPoolTabValid(boolean value)
    {
        if (isPoolTabValid != value)
        {
            isPoolTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsPoolTabValid")); //$NON-NLS-1$
        }
    }

    private NotChangableForVmInPoolEntityModel<Integer> prestartedVms;

    public EntityModel<Integer> getPrestartedVms() {
        return prestartedVms;
    }

    protected void setPrestartedVms(NotChangableForVmInPoolEntityModel<Integer> value) {
        prestartedVms = value;
    }

    private String prestartedVmsHint;

    public String getPrestartedVmsHint() {
        return prestartedVmsHint;
    }

    public void setPrestartedVmsHint(String value) {
        if (!ObjectUtils.objectsEqual(prestartedVmsHint, value)) {
            prestartedVmsHint = value;
            onPropertyChanged(new PropertyChangedEventArgs("PrestartedVmsHint")); //$NON-NLS-1$
        }
    }

    private NotChangableForVmInPoolEntityModel<Integer> maxAssignedVmsPerUser;

    public EntityModel<Integer> getMaxAssignedVmsPerUser() {
        return maxAssignedVmsPerUser;
    }

    public void setMaxAssignedVmsPerUser(NotChangableForVmInPoolEntityModel<Integer> maxAssignedVmsPerUser) {
        this.maxAssignedVmsPerUser = maxAssignedVmsPerUser;
    }

    private class NotChangableForVmInPoolListModel<T> extends ListModel<T> {
        @Override
        public ListModel<T> setIsChangable(boolean value) {
            if (!isVmAttachedToPool()) {
                super.setIsChangable(value);
            }
            return this;
        }
    }

    private class NotChangableForVmInPoolEntityModel<T> extends EntityModel<T> {
        public NotChangableForVmInPoolEntityModel() {
        }

        public NotChangableForVmInPoolEntityModel(T entity) {
            super(entity);
        }

        @Override
        public EntityModel<T> setIsChangable(boolean value) {
            if (!isVmAttachedToPool()) {
                super.setIsChangable(value);
            }
            return this;
        }
    }

    private class NotChangableForVmInPoolKeyValueModel extends KeyValueModel {
        @Override
        public KeyValueModel setIsChangable(boolean value) {
            if (!isVmAttachedToPool()) {
                super.setIsChangable(value);
            }
            return this;
        }
    }

    private ListModel<String> watchdogModel;

    public ListModel<String> getWatchdogModel() {
        return watchdogModel;
    }

    public void setWatchdogModel(ListModel<String> watchdogModel) {
        this.watchdogModel = watchdogModel;
    }

    private ListModel<String> watchdogAction;

    public ListModel<String> getWatchdogAction() {
        return watchdogAction;
    }

    public void setWatchdogAction(ListModel<String> watchdogAction) {
        this.watchdogAction = watchdogAction;
    }

    public StoragePool getSelectedDataCenter() {
        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return null;
        }

        return dataCenterWithCluster.getDataCenter();
    }

    public VDSGroup getSelectedCluster() {
        DataCenterWithCluster dataCenterWithCluster = getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return null;
        }

        return dataCenterWithCluster.getCluster();
    }

    public void disableEditing(String msg) {
        getDefaultCommand().setIsExecutionAllowed(false);
        getEditingEnabled().setMessage(msg);
        getEditingEnabled().setEntity(false);
    }

    public static enum CpuSharesAmount {
        DISABLED(0), LOW(512), MEDIUM(1024), HIGH(2048), CUSTOM(-1);

        private int value;
        private CpuSharesAmount(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    public Integer getSelectedMigrationDowntime() {
        if (Boolean.TRUE.equals(getOverrideMigrationDowntime().getEntity())) {
            return getMigrationDowntime().getEntity();
        } else {
            return null;
        }
    }

    public void setSelectedMigrationDowntime(Integer value) {
        getOverrideMigrationDowntime().setEntity(value != null);
        getMigrationDowntime().setEntity(value);
    }
}
