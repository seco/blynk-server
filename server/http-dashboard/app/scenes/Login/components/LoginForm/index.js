import React from 'react';

import {Button, Form, Alert} from 'antd';

import {reduxForm} from 'redux-form';

import {Field as FormField} from 'components/Form';

import Validation from 'services/Validation';

import './styles.scss';

@reduxForm({
  form: 'Login'
})
export default class LoginForm extends React.Component {

  static propTypes = {
    invalid: React.PropTypes.bool,
    pristine: React.PropTypes.bool,
    submitting: React.PropTypes.bool,
    handleSubmit: React.PropTypes.func,
    error: React.PropTypes.string,
    loading: React.PropTypes.bool,
    handleForgotPass: React.PropTypes.func
  };

  render() {

    const {invalid, pristine, handleSubmit, error, submitting, handleForgotPass} = this.props;

    const FormItem = Form.Item;

    return (<Form onSubmit={handleSubmit.bind(this)}>
      <FormItem>
        <span className="form-header">Log in</span>
      </FormItem>

      <FormField type="text" name="email"
                 icon="user"
                 placeholder="Email"
                 displayError={false}
                 validate={[
                   Validation.Rules.required,
                   Validation.Rules.email
                 ]}/>

      <FormField type="password" name="password"
                 icon="lock"
                 placeholder="Password"
                 displayError={false}
                 validate={[
                   Validation.Rules.required
                 ]}/>

      <FormItem className="login-alert">
        { error && <Alert description={error} type="error"/> }
      </FormItem>

      <FormItem>
        <Button type="primary"
                loading={submitting || this.props.loading}
                htmlType="submit" className="login-form-button"
                disabled={invalid || pristine || submitting}>
          Log in
        </Button>

      </FormItem>
      <FormItem>
        <a className="login-form-forgot" onClick={handleForgotPass.bind(this)}>Forgot password?</a>
      </FormItem>
    </Form>);
  }
}
