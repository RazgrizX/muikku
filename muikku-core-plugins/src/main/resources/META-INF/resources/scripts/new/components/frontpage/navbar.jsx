import Navbar from '../general/navbar.jsx';
import Link from '../general/link.jsx';
import LoginButton from '../base/login-button.jsx';
import ForgotPasswordDialog from '../base/forgot-password-dialog.jsx';

class FrontpageNavbar extends React.Component {
  constructor(props){
    super(props);
  }
  render(){
    return <Navbar classNameExtension="frontpage" navbarItems={[
      {
        classNameSuffix: "studying",
        item: (<Link href="#studying" className="link link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.studying')}</span></Link>)
      },
      {
        classNameSuffix: "news",
        item: (<Link href="#news" className="link link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.news')}</span></Link>)
      },
      {
        classNameSuffix: "otavan-opisto",
        item: (<Link href="#organization" className="link link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.otavanOpisto')}</span></Link>)
      },
      {
        classNameSuffix: "contact",
        item: (<Link href="#contact" className="link link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.contact')}</span></Link>)
      },
      {
        classNameSuffix: "open-materials",
        item: (<Link href="/coursepicker" className="link link-highlight link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.openMaterials')}</span></Link>)
      }
    ]} defaultOptions={[
      (<LoginButton key="0" classNameExtension="frontpage"/>),
      (<ForgotPasswordDialog key="1" classNameExtension="frontpage"><Link className="frontpage label label-dynamic-word-break label-clickable frontpage-label-forgot-password frontpage-interact-forgot-password">
         <span>{this.props.i18n.text.get('plugin.forgotpassword.forgotLink')}</span>
       </Link></ForgotPasswordDialog>)
    ]} menuItems={[
       (<Link href="#studying" className="link link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.studying')}</span></Link>),
       (<Link href="#news" className="link link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.news')}</span></Link>),
       (<Link href="#organization" className="link link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.otavanOpisto')}</span></Link>),
       (<Link href="#contact" className="link link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.contact')}</span></Link>),
       (<Link href="/coursepicker" className="link link-highlight link-full"><span>{this.props.i18n.text.get('plugin.navigation.link.openMaterials')}</span></Link>)
    ]}/>
  }
}

function mapStateToProps(state){
  return {
    i18n: state.i18n
  }
};

const mapDispatchToProps = (dispatch)=>{
  return {};
};

export default ReactRedux.connect(
  mapStateToProps,
  mapDispatchToProps
)(FrontpageNavbar);
