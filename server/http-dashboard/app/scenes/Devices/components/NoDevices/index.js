import React from 'react';
import { Link } from 'react-router';
import { DeviceCreateModal } from 'scenes/Devices/scenes';
import { Button } from 'antd';
import './styles.less';
import { VerifyPermission, PERMISSIONS_INDEX } from "services/Roles";

class NoDevices extends React.Component {

  static contextTypes = {
    router: React.PropTypes.object
  };

  static propTypes = {
    location: React.PropTypes.object,
    isAnyProductExist: React.PropTypes.bool,
    organization: React.PropTypes.object,
    permissions: React.PropTypes.number,
  };

  state = {
    isDeviceCreateModalVisible: false
  };

  componentWillMount() {
    this.checkModalVisibility();
  }

  componentDidUpdate() {
    this.checkModalVisibility();
  }

  onDeviceCreateModalClose() {
    this.context.router.push('/devices');
  }

  checkModalVisibility() {
    if (this.props.location.pathname.indexOf('create') !== -1 && !this.state.isDeviceCreateModalVisible) {
      this.setState({
        isDeviceCreateModalVisible: true
      });
    } else if (this.props.location.pathname.indexOf('create') === -1 && this.state.isDeviceCreateModalVisible) {
      this.setState({
        isDeviceCreateModalVisible: false
      });
    }
  }

  renderNoDevice() {
    if (this.props.organization.parentId !== -1) {
      return (
        <div>
          <div className="devices-no-items-description">
            Here you will find a list of all of your activated devices and their
            data visualized
          </div>
        </div>
      );
    } else {
      return (
        <div>
          <div className="devices-no-items-description">
            You don’t have any devices yet. <br/>
            Start with creating a New Product
          </div>
          {VerifyPermission(this.props.permissions, PERMISSIONS_INDEX.PRODUCT_CREATE) &&
          <div className="devices-no-items-action">
            <Link to="/products/create">
              <Button icon="plus" type="primary">New Product</Button>
            </Link>
          </div>}
        </div>
      );
    }

  }

  render() {

    return (
      <div className="devices">
        <div className="devices-no-items">
          <div className="devices-no-items-title">
            All of your devices and their data will be here.
          </div>
          {!this.props.isAnyProductExist && this.renderNoDevice() || (
            <div>
              <div className="devices-no-items-description">
                Here you will find a list of all of your activated devices and
                their data visualized
              </div>
              {VerifyPermission(this.props.permissions, PERMISSIONS_INDEX.OWN_DEVICES_CREATE)
              && process.env.BLYNK_CREATE_DEVICE
              && JSON.parse(process.env.BLYNK_CREATE_DEVICE)
              && <div className="devices-no-items-action">
                <Link to="/devices/create">
                  <Button icon="plus" type="primary">Create New Device</Button>
                </Link>
              </div>}

              <DeviceCreateModal visible={this.state.isDeviceCreateModalVisible}
                                 onClose={this.onDeviceCreateModalClose.bind(this)}/>

            </div>
          )}

        </div>
      </div>
    );
  }

}

export default NoDevices;
